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
	//private ArrayList<Player> playersToDelete;

	public ThreadRequestsHandler(Socket connection){
		player = SingletonFactory.getPlayerSingleton();
		player_name = player.getName();
		target = SingletonFactory.getWebTargetSingleton();
		conn = connection;
		game = SingletonFactory.getGameSingleton();
		moves = SingletonFactory.getSingletonMoves();
		explodedBombs = SingletonFactory.bombExploded();
		playersToAdd = SingletonFactory.getPlayersToAdd();
		//playersToDelete = SingletonFactory.getPlayersToDelete();
		try{
			inFromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			outToClient = new DataOutputStream(conn.getOutputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void run(){
		String response = "";
		while(true){
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
		case "end":
			notEntering();
		default:
			break;
		}
	}

	//handler per la mossa (movimento o bomba)
	private void myTurn() {
		checkMyExplosions();
		checkVictory();
		checkEnteringPlayers();
		checkIfImDead();
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
		checkVictory();
		forwardToken();
	}


	private void checkIfImDead() {
		if(player.isDead() && player.getActiveBombs()==0){ // ho finito tutto quello che devo fare
			deletePlayer();
		}else if(player.isDead()){ // ho ancora bombe da far esplodere, ma sono morto
			forwardToken();
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
		player.removeActiveBomb();
		System.out.print("[INFO] Bomba "+b.getColor()+" esplosa!\n");
		Position []area = game.getArea(b.getColor());
		if(player.isInArea(area) && !player.isDead()){
			System.out.println("[INFO] Sei morto a causa della tua stessa bomba!");
			player.killPlayer();
		}
		sendRequestToAll("explosion", new boolean[1], b);
	}

	// prima di fare la mossa controllo che non ci siano giocatori che vogliono entrare
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
	}

	//handler per la bomba
	private void bomb(Bomb b) {
		System.out.println("[INFO] Bomba "+b.getColor()+" lanciata!");
		player.addActiveBomb();
		sendRequestToAll("bomb", new boolean[1], b.getColor());
		ThreadBombExplosion bombEx = new ThreadBombExplosion(b);
		bombEx.start();
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
		System.out.println(game.getPosOnGameArea(player.getPos()));
	}

	// sblocco il giocatore che ha il token, visto che il giocatore è entrato
	private void unlockThePlayerWithTheToken() {
		synchronized(playersToAdd){
			playersToAdd.notifyAll();
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
		String content = getContentFromStringResponse(response);
		if(response.split(" ")[0].equals("accepted"))
		{
			synchronized(playersToAdd){
				for(int i=0;i<playersToAdd.size();i++){
					if(playersToAdd.get(i).getName().equals(pl.getName()))
						playersToAdd.remove(i);
				}
			}
			socketHandlerWriter(player.marshallerThis()+"\n");
			if(content.equals(player_name))
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
		if(player.getMy_next().equals(pl_name))
		{
			player.setMy_next(pl.getMy_next());
		}
		game.removePlayer(pl_name);
		System.out.println("\n[INFO] Notifica cancellazione giocatore! ["+pl_name+"]");
		socketHandlerWriter("ok\n");
	}

	//handler per il controllo della posizione inviata da altro giocatore
	private void checkPos(String content) {
		Position pos = player.getPos();
		StringReader reader;
		Position position;
		reader = new StringReader(content);
		position = Position.unmarshallThat(reader);
		if(pos.equals(position) && !player.isDead()){
			player.killPlayer();
			socketHandlerWriter("colpito "+player.getMy_next()+"\n");
			System.out.println("[INFO] Sei stato eliminato!");
		}else{
			socketHandlerWriter("mancato \n");
		}
	}

	// metodo per controllare se l'esplosione mi ha fatto fuori
	private void checkExplosion(String color) {
		System.out.print("[INFO] Bomba "+color+" esplosa!");
		Position []area = game.getArea(color);
		boolean checkEx = player.isInArea(area);
		if(checkEx && !player.isDead()){
			player.killPlayer();
			socketHandlerWriter("colpito "+player.getMy_next()+"\n");
			System.out.println("[INFO] Sei stato eliminato!");
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

	// parsing del messaggio
	private String getContentFromStringResponse(String content) {
		int length = content.length();
		int index = content.indexOf("CONTENT:");
		return content.substring(index+8, length);
	}


	// handler per la scrittura sulla socket
	private void socketHandlerWriter(String message){
		try {
			outToClient.writeBytes(message);
		} catch (IOException e) {

		}
	}
	
	// handelr per la lettura sulla socket
	private String socketHandlerReader(){
		String response = null;
		try {
			response = inFromClient.readLine();
		} catch (IOException e) {

		}
		return response;
	}

	// metodo per controllare se il giocatore ha vinto
	private void checkVictory() {
		if(!player.isDead() && player.getPoints()>=game.getMax_point())
		{
			target.path("deletegame").path(game.getGame_name()).request().delete();
			sendRequestToAll("victory", new boolean[1], new Object());
			sendKoToEnteringPlayers();
			System.out.println("[INFO] Hai vinto!");
			player.killPlayer();
		}
	}
	
	// metodo per uscire dalla partita se questa si è conclusa durante il mio inserimento
	private void notEntering() {
		System.out.println("[INFO] La partita si è conclusa! Mi spiace.");
		System.exit(0);
		
	}

	// metodo per cancellare il giocatore
	private void deletePlayer() {
		target.path("deleteplayer").path(game.getGame_name()).path(player_name).request().delete();
		sendRequestToAll("deleteplayer", new boolean[1], player);
		System.out.println("[INFO] Fine partita.");
		if(!player.getMy_next().equals(player_name))
			forwardToken();
		System.exit(0);
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
	
	private void sendKoToEnteringPlayers() {
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		for(Player pl_i: playersToAdd){
			ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, "end", null, null);
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
}
