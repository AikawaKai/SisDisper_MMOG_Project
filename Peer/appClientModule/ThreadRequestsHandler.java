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

	public ThreadRequestsHandler(WebTarget target_, Socket connection){
		player = SingletonFactory.getPlayerSingleton();
		player_name = player.getName();
		target = target_;
		conn = connection;
		game = SingletonFactory.getGameSingleton();
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
			try {
				response = inFromClient.readLine();
				if(response==null)// response = HEADER CONTENT:
					break;
				requestsHandler(response);
			} catch (IOException e) {
				break;
			} 
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
			checkPosBomb(content);
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
		BufferMoves moves = SingletonFactory.getSingletonMoves();
		Move m = null;
		synchronized(moves){
			m = moves.getFirst();
			if(m==null)
				forwardToken();
			else{
				if(m instanceof BasicMove)
					basicMove((BasicMove) m);
				else
					bomb((Bomb) m);
			}
		}
	}

	//handler per l'aggiunta di un giocatore
	private void playersUpdate(String player_string) {
		String response = "";
		StringReader reader = null;
		Player pl;
		String pl_name;
		try {
			reader = new StringReader(player_string);
			pl = Player.unmarshallThat(reader);
			pl_name = pl.getName();
			if(pl.getPos().equals(player.getPos()))
				outToClient.writeBytes("ko\n");
			else
			{
				outToClient.writeBytes("ok\n");
			}
			response = inFromClient.readLine();
			if(response.equals("accepted"))
			{
				response = inFromClient.readLine();
				outToClient.writeBytes(player.marshallerThis()+"\n");
				if(response.equals(player_name))
					player.setMy_next(pl_name);
				System.out.println("[INFO] Notifica nuovo giocatore!");
				game.addPlayer(pl);
				System.out.println("["+pl_name+"]");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	//handler per la cancellazione di un giocatore in partita
	private void playersUpdateDelete(String content) {
		StringReader reader;
		Player pl;
		String pl_name;
		try {
			reader = new StringReader(content);
			pl = (Player) Player.unmarshallThat(reader);
			pl_name = pl.getName();
			if(player.getMy_next().equals(pl_name))
			{
				player.setMy_next(pl.getMy_next());
			}
			game.removePlayer(pl_name);
			outToClient.writeBytes("accepted\n");
			System.out.println("[INFO] Notifica cancellazione giocatore!");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	//handler per il controllo della posizione inviata da altro giocatore
	private void checkPos(String content) {
		Position pos = player.getPos();
		StringReader reader;
		Position position;
		try {
			reader = new StringReader(content);
			position = Position.unmarshallThat(reader);
			if(pos.equals(position)){
				System.out.println("[INFO] Eliminato");
				outToClient.writeBytes("colpito "+player.getMy_next()+"\n"+"\n");
				sendRequestDeletePlayer();
			}else{
				outToClient.writeBytes("mancato \n");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	// metodo per il controllo dell'area di esplosione della bomba
	private void checkPosBomb(String color) {
		Position []area = game.getArea(color);
		System.out.println("[INFO] Bomba "+color+" lanciata!");
		try {
			outToClient.writeBytes("ok\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// metodo per la sconfitta
	private void admitDefeat() {
		System.out.println("[INFO] Hai perso! Mi spiace.");
		target.path("deleteplayer").path(game.getGame_name()).path(player_name).request().delete();

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

	// mi muovo nella direzione specificata in m
	private void basicMove(BasicMove m) {
		move(m.getMovement());
		System.out.println("[INFO] Ti sei spostato");
		System.out.println(game.getPosOnGameArea(player.getPos()));
		sendRequestToAll("sendnewpos", new boolean[1], new Object());
		if(!player.isDead() && player.getPoints()>=game.getMax_point())
		{
			sendRequestToAll("victory", new boolean[1], new Object());
			System.out.println("Hai vinto!");
		}else{
			forwardToken();
		}
	}

	//funzione che esegue la mossa movimento
	private void move(String movement) {
		Position pos = player.getPos();
		int old_x = pos.getPos_x();
		int old_y = pos.getPos_y();
		int game_size = game.getSize_x();
		switch(movement){
		case "w":
			if(old_x-1>=0){
				pos.setPos_x(old_x-1);
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		case "x":
			if(old_x+1<game_size){
				pos.setPos_x(old_x+1);
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		case "a":
			if(old_y-1>=0){
				pos.setPos_y(old_y-1);
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		case "d":
			if(old_y+1<game_size){
				pos.setPos_y(old_y+1);
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		}
	}

	//funzione per mandare la richiesta di cancellazione al server e agli altri peer
	private void sendRequestDeletePlayer() {
		target.path("deleteplayer").path(game.getGame_name()).path(player_name).request().delete();
		sendRequestToAll("deleteplayer", new boolean[1], new Object());
	}

	//handler per la bomba
	private void bomb(Bomb b) {
		System.out.println("[INFO] Bomba "+b.getColor()+" lanciata!");
		sendRequestToAll("bomb", new boolean[1], b.getColor());
		forwardToken();
	}

	// parsing del messaggio
	private String getContentFromStringResponse(String content) {
		int length = content.length();
		int index = content.indexOf("CONTENT:");
		return content.substring(index+8, length);
	}

	//manda la richiesta a tutti eccetto me stesso
	private void sendRequestToAll(String request, boolean[] check, Object objectToSend) {
		String player_name = SingletonFactory.getPlayerSingleton().getName();
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		for(Player pl_i: game.getPlayers()){
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

}
