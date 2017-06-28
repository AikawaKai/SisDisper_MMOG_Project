import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

import peer.objects.BasicMove;
import peer.objects.Bomb;
import peer.objects.BufferMoves;
import peer.objects.Game;
import peer.objects.Move;
import peer.objects.Player;
import peer.objects.Position;
import peer.objects.SingletonFactory;

//------------------------------------------------------------------------------------ 
//						        [PEER SERVER THREAD]                                        
//
//            Thread handler per le diverse richieste che arrivano al peer   
//-------------------------------------------------------------------------------------

public class ThreadRequestsHandler extends Thread{
	private Player player;
	private String player_name;
	private Game game;
	private Socket conn;
	private WebTarget target;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private BufferMoves moves;
	private ArrayList<Bomb> explodedBombs;
	private ArrayList<Player> playersToAdd;
	private ArrayList<Player> playersToDelete;

	public ThreadRequestsHandler(Socket connection){
		player = SingletonFactory.getPlayerSingleton();
		player_name = player.getName();
		target = SingletonFactory.getWebTargetSingleton();
		conn = connection;
		game = SingletonFactory.getGameSingleton();
		moves = SingletonFactory.getSingletonMoves();
		explodedBombs = SingletonFactory.bombExploded();
		playersToAdd = SingletonFactory.getPlayersToAdd();
		playersToDelete = SingletonFactory.getPlayersToDelete();
		try{
			inFromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			outToClient = new DataOutputStream(conn.getOutputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void run(){
		String response = "";
		while(true && !player.isDead()){
			response = socketHandlerReader();
			if(response==null)// response = HEADER CONTENT:
				break;
			requestsHandler(response);
		}
	}


	//handler per le richieste da gestire che arrivano al peer
	public void requestsHandler(String response) {
		String []parts = response.split(" ");
		String header = parts[0];
		String content = null;
		if(parts.length>1){
			content = getContentFromStringResponse(response);
		}
		switch(header){
		case "token":
			myTurn();
			break;
		case "checkin":
			checkIn(content);
			break;
		case "start":
			comeInNewPlayer();
			break;
		case "imin":
			unlockThePlayerWithTheToken();
			break;
		case "newplayer":
			playersUpdate(content);
			break;
		case "deleteplayer":
			playersUpdateDelete(content);
			break;
		case "newpos":
			checkPos(content);
			break;
		case "bomb":
			notifyBomb(content);
			break;
		case "explosion":
			checkExplosion(content);
			break;
		case "victory":
			admitDefeat();
			break;
		default:
			break;
		}
	}

	//handler per la mossa (movimento o bomba)
	private void myTurn() {
		checkMyExplosions();
		checkDeletedPlayers();
		checkEnteringPlayers();
		Move m = null;
		synchronized(moves){
			m = moves.getFirst();
		}
		if(m==null){
			forwardToken();
			return;
		}
		if(m instanceof BasicMove)
			basicMove((BasicMove) m);
		else
			bomb((Bomb) m);
	}

	private void checkDeletedPlayers() {
		synchronized(playersToDelete){
			if(playersToDelete.isEmpty())
			{
				return;
			}
			while(playersToDelete.size()!=0){
				Player first = playersToDelete.remove(0);
				game.removePlayer(first.getName());
				for(Player pl:playersToDelete){
					if(pl.getMy_next().equals(first.getName()))
						pl.setMy_next(first.getMy_next());
				}
				if(player.getMy_next().equals(first.getName()))
					player.setMy_next(first.getMy_next());
			}

		}
	}

	//handler per il controllo delle esplosioni causate da me
	private void checkMyExplosions() {
		Bomb b=null;
		synchronized(explodedBombs){
			if(explodedBombs.size()>0)
				b=explodedBombs.remove(0);
		}
		if(b==null)
			return;
		System.out.print("[INFO] Bomba "+b.getColor()+" esplosa!\n");
		Position []area = game.getArea(b.getColor());
		if(player.isInArea(area)){
			System.out.println("[INFO] Sei morto a causa della tua stessa bomba!");
			player.killPlayer();
			sendRequestDeletePlayer();
		}
		sendRequestToAll("explosion", new boolean[1], b);
		WebTarget target = SingletonFactory.getWebTargetSingleton();
		if(!player.isDead() && player.getPoints()>=game.getMax_point())
		{
			target.path("deletegame").path(game.getGame_name()).request().delete();
			sendRequestToAll("victory", new boolean[1], new Object());
			System.out.println("[INFO] Hai vinto!");
			player.killPlayer();
		}

	}

	// prima di fare la mossa controllo che non ci siano giocatore che vogliono entrare
	private void checkEnteringPlayers() {
		Player enterPl;
		synchronized(playersToAdd){
			if(playersToAdd.isEmpty())
			{
				return;
			}
			enterPl = playersToAdd.remove(0);
		}
		enterPl.sendMessage("start\n");
		synchronized(playersToAdd){
			try {
				playersToAdd.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// mi muovo nella direzione specificata in m
	private void basicMove(BasicMove m) {
		m.move(player, game);
		System.out.println(game.getPosOnGameArea(player.getPos()));
		sendRequestToAll("sendnewpos", new boolean[1], new Object());
		if(!player.isDead() && player.getPoints()>=game.getMax_point())
		{
			target.path("deletegame").path(game.getGame_name()).request().delete();
			sendRequestToAll("victory", new boolean[1], new Object());
			System.out.println("[INFO] Hai vinto!");
			player.killPlayer();
		}else{
			forwardToken();
		}
	}

	//handler per la bomba
	private void bomb(Bomb b) {
		System.out.println("[INFO] Bomba "+b.getColor()+" lanciata!");
		sendRequestToAll("bomb", new boolean[1], b.getColor());
		ThreadBombExplosion bombEx = new ThreadBombExplosion(b);
		bombEx.start();
		forwardToken();
	}

	// aggiunge alla coda dei player da aggiungere
	private void checkIn(String player) {
		Player toAdd = Player.unmarshallThat(new StringReader(player));
		synchronized(playersToAdd){
			playersToAdd.add(toAdd);
		}
	}

	//funzione che si occupa di generare una nuova posizione random per il nuovo giocatore
	//confermandola agli altri peer
	private void comeInNewPlayer() {
		// controllo la posizione randomica all'ingresso
		boolean check[] = {true};
		ArrayList<Player> players_deleted = new ArrayList<Player>();
		while(check[0]){
			check[0] = false;
			Position pos = game.genRandPosition();
			player.setPos(pos);
			sendRequestToAll("newplayer", check, players_deleted);
			if(check[0])
			{
				for(Player pl_to_del: players_deleted){
					game.removePlayer(pl_to_del.getName());
				}
				sendRequestToAll("notaccepted", check, new Object());
			}
		}
		//scelgo il mio next
		Player player_next;
		int num_players = game.numPlayers();
		int choose = Main.randInt(0, num_players-1);
		player_next = game.getPlayers().get(choose);
		while(player_next.equals(player) && num_players>1){
			choose = Main.randInt(0, num_players-1);
			player_next = game.getPlayers().get(choose);
		}
		// sono riuscito a notificare la mia posizione senza conflitti, la faccio accettare a tutti
		sendRequestToAll("accepted", check, player_next);
		sendRequestToAll("imin", new boolean[1], new Object());
	}

	// sblocco il giocatore che ha il token, visto che il giocatore è entrato
	private void unlockThePlayerWithTheToken() {
		synchronized(playersToAdd){
			playersToAdd.notify();
		}
	}

	//handler per l'aggiunta di un giocatore
	private void playersUpdate(String player_string) {
		String response = "";
		StringReader reader = null;
		Player pl;
		String pl_name;
		reader = new StringReader(player_string);
		pl = Player.unmarshallThat(reader);
		pl_name = pl.getName();
		if(pl.getPos().equals(player.getPos()))
			socketHandlerWriter("ko\n");
		else
		{
			socketHandlerWriter("ok\n");
		}
		response = socketHandlerReader();
		if(response.equals("accepted"))
		{
			synchronized(playersToAdd){
				for(int i=0;i<playersToAdd.size();i++){
					if(playersToAdd.get(i).getName().equals(pl.getName()))
						playersToAdd.remove(i);
				}
			}
			response = socketHandlerReader();
			socketHandlerWriter(player.marshallerThis()+"\n");
			if(response.equals(player_name))
				player.setMy_next(pl_name);
			System.out.println("[INFO] Notifica nuovo giocatore!"+"["+pl_name+"]");
			game.addPlayer(pl);
		}
	}

	//handler per la cancellazione di un giocatore in partita
	private void playersUpdateDelete(String content) {
		StringReader reader;
		Player pl;
		String pl_name;
		reader = new StringReader(content);
		pl = (Player) Player.unmarshallThat(reader);
		pl_name = pl.getName();
		synchronized(playersToDelete){
			playersToDelete.add(pl);
		}
		if(player.getMy_next().equals(pl_name))
		{
			player.setMy_next(pl.getMy_next());
		}
		System.out.println("\n[INFO] Notifica cancellazione giocatore! ["+pl_name+"]");
	}

	//handler per il controllo della posizione inviata da altro giocatore
	private void checkPos(String content) {
		Position pos = player.getPos();
		StringReader reader;
		Position position;
		reader = new StringReader(content);
		position = Position.unmarshallThat(reader);
		if(pos.equals(position)){
			socketHandlerWriter("colpito "+player.getMy_next()+"\n");
			sendRequestDeletePlayer();
			player.killPlayer();
			player.closeSocket();
			System.out.println("[INFO] Eliminato");
		}else{
			socketHandlerWriter("mancato \n");
		}
	}

	// metodo per controllare se l'esplosione mi ha fatto fuori
	private void checkExplosion(String color) {
		System.out.print("[INFO] Bomba "+color+" esplosa!");
		Position []area = game.getArea(color);
		boolean checkEx = player.isInArea(area);
		if(checkEx){
			socketHandlerWriter("colpito "+player.getMy_next()+"\n");
			sendRequestDeletePlayer();
			player.killPlayer();
			player.closeSocket();
			System.out.println("[INFO] Eliminato");
		}else{
			socketHandlerWriter("mancato \n");
		}
	}

	// metodo per la notifica dell'attivazione di una bomba
	private void notifyBomb(String color) {
		//Position []area = game.getArea(color);
		System.out.println("[INFO] Bomba "+color+" lanciata!");
		socketHandlerWriter("ok\n");
	}

	// metodo per la sconfitta
	private void admitDefeat() {
		System.out.println("[INFO] Hai perso! Mi spiace.");
		player.killPlayer();
		player.closeSocket();
	}

	// manda il token al mio next
	private void forwardToken() {
		ThreadSendRequestToPlayer forwardToken = new ThreadSendRequestToPlayer(game.getPlayer(player.getMy_next()), "token", new boolean[1], new Object());
		forwardToken.start();
		try {
			forwardToken.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//funzione per mandare la richiesta di cancellazione al server e agli altri peer
	private void sendRequestDeletePlayer() {
		target.path("deleteplayer").path(game.getGame_name()).path(player_name).request().delete();
		sendRequestToAll("deleteplayer", new boolean[1], player);
	}

	// parsing del messaggio
	private String getContentFromStringResponse(String content) {
		int length = content.length();
		int index = content.indexOf("CONTENT:");
		return content.substring(index+8, length);
	}

	//manda la richiesta a tutti eccetto me stesso
	void sendRequestToAll(String request, boolean[] check, Object objectToSend) {
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		ArrayList<Player> players_copy = game.getPlayersCopy();
		for(Player pl_i: players_copy){
			if(pl_i.getName().equals(player_name))
				continue;
			ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, request, check, objectToSend);
			threads.add(pl_hl);
			pl_hl.start();
		}
		for(ThreadSendRequestToPlayer hl: threads){
			try {
				hl.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void socketHandlerWriter(String message){
		try {
			outToClient.writeBytes(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String socketHandlerReader(){
		String response = null;
		try {
			response = inFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

}
