import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

import peer.objects.BasicMove;
import peer.objects.Bomb;
import peer.objects.BufferMoves;
import peer.objects.Game;
import peer.objects.Move;
import peer.objects.Player;
import peer.objects.Position;

//------------------------------------------------------------------------------------ 
//						        [PEER SERVER THREAD]                                        
//
//            Thread handler per le diverse richieste che arrivano al peer   
//-------------------------------------------------------------------------------------

public class ThreadRequestsHandler extends Thread{
	
	private Game g;
	private String player_name;
	private Player player;
	private Socket conn;
	private WebTarget target;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	private BufferMoves moves;
	
	public ThreadRequestsHandler(BufferMoves m, WebTarget target_, Socket connection, String my_name, Game game){
		conn = connection;
		g = game;
		player_name = my_name;
		player = g.getPlayer(player_name);
		target = target_;
		moves = m;
		try{
			inFromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        outToClient = new DataOutputStream(conn.getOutputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void run(){
		String response = "";
		String []parts;
		while(true){
			try {
	            response = inFromClient.readLine();
	            // response = HEADER CONTENT
	            if(response==null)
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
			content = getContentFromArray(response);
		}
		switch(header){
		case "newplayer":
			playersUpdate(content);
			break;
		case "deleteplayer":
			playersUpdateDelete(content);
			break;
		case "token":
			myTurn();
			break;
		case "newpos":
			checkPos(content);
			break;
		default:
			break;
		}
	}

	private String getContentFromArray(String content) {
		int length = content.length();
		int index = content.indexOf("CONTENT:");
		return content.substring(index+8, length);
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
					g.addPlayer(pl);
					System.out.println("["+pl_name+"]");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	//handler per la cancellazione di un giocatore in partita
	private void playersUpdateDelete(String content) {
		String response = "";
		StringReader reader;
		Player pl;
		String pl_name;
		try {
			reader = new StringReader(content);
			pl = (Player) Player.unmarshallThat(reader);
			pl_name = pl.getName();
			synchronized(g){
				if(player.getMy_next().equals(pl_name))
				{
					player.setMy_next(pl.getMy_next());
				}
				g.removePlayer(pl_name);
			}
			outToClient.writeBytes("accepted\n");
			System.out.println("[INFO] Notifica cancellazione giocatore!");
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	//handler per la mossa (movimento o bomba)
	private void myTurn() {
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

	private void forwardToken() {
		ThreadSendRequestToPlayer forwardToken = new ThreadSendRequestToPlayer(player, g.getPlayer(player.getMy_next()), "token", new boolean[1], new Object());
		forwardToken.start();
		try {
			forwardToken.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void basicMove(BasicMove m) {
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		move(m.getMovement());
		System.out.println("[INFO] Ti sei spostato");
		System.out.println(g.getPosOnGameArea(player.getPos()));
		for(Player pl_i: g.getPlayers()){
			if(!pl_i.getName().equals(player_name)){
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "sendnewpos", new boolean[1], new Object());
				threads.add(pl_hl);
				pl_hl.start();
			}
		}

		for(ThreadSendRequestToPlayer pl_hl: threads){
			try {
				pl_hl.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//aspettiamo a forwardare il token...
		forwardToken();
	}
	
	//funzione che esegue la mossa movimento
	private void move(String movement) {
		Position pos = player.getPos();
		int old_x = pos.getPos_x();
		int old_y = pos.getPos_y();
		int game_size = g.getSize_x();
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
	
	
	
	//funzione per mandare la richiesta di cancellazione al server e agli altri peer
	private void sendRequestDeletePlayer() {
		target.path("deleteplayer").path(g.getGame_name()).path(player_name).request().delete();
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		for(Player pl_i: g.getPlayers()){
			if(!pl_i.getName().equals(player_name))
			{
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "deleteplayer", new boolean[1], new Object());
				threads.add(pl_hl);
				pl_hl.start();
			}
		}
		for(ThreadSendRequestToPlayer pl_hl: threads){
			try {
				pl_hl.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//handler per la bomba
	private void bomb(Bomb b) {
		
	}
}
