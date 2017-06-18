import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;

import peer.objects.Game;
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
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	
	public ThreadRequestsHandler(Socket connection, String my_name, Game game){
		conn = connection;
		g = game;
		player_name = my_name;
		player = g.getPlayer(player_name);
		try{
			inFromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        outToClient = new DataOutputStream(conn.getOutputStream());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void run(){
		String response="";
		while(true){
			try {
	            response = inFromClient.readLine();
	            requestsHandler(response);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	}

	
	//handler per le richieste da gestire che arrivano al peer
	public void requestsHandler(String response) {
		switch(response){
		case "newplayer":
			playersUpdate();
			break;
		case "deleteplayer":
			System.out.println("[INFO] Notifica cancellazione giocatore!");
			playersUpdateDelete();
			break;
		case "token":
			System.out.println("[INFO] È il tuo turno! Fai una mossa!");
			doMove();
			break;
		case "newpos":
			checkPos();
			break;
		default:
			break;
		}
	}
	
	//handler per la mossa (movimento o bomba)
	private void doMove() {
		System.out.println(player.getPos());
		System.out.println(g.getPosOnGameArea(player.getPos()));
		int scelta=0;
		while(scelta!=1 && scelta!=2){
			System.out.println("Scegli cosa fare:");
			System.out.println("1 - muoviti in una direzione");
			System.out.println("2 - Usa una bomba (se ne possiedi una)");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Scelta: ");
			scelta = integerReaderHandler(bufferedReader);
		}
		switch(scelta){
		case 1:
			ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
			move(player.getPos());
			System.out.println("Spostato:");
			System.out.println(g.getPosOnGameArea(player.getPos()));
			for(Player pl_i: g.getPlayers()){
				if(!pl_i.getName().equals(player_name)){
					ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "sendnewpos", new boolean[1]);
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
			ThreadSendRequestToPlayer forwardToken = new ThreadSendRequestToPlayer(player, g.getPlayer(player.getMy_next()), "token", new boolean[1]);
			forwardToken.start();
			try {
				forwardToken.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			bomb();
		default:
			break;
		}
		System.out.println("Fine turno.");
	}
	
	//handler per il controllo della posizione inviata da altro giocatore
	private void checkPos() {
		Position pos = player.getPos();
		String response = "";
		StringReader reader;
		Position position;
		try {
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
			position = Position.unmarshallThat(reader);
			System.out.println(position);
			if(pos.equals(position)){
				System.out.println("[INFO] Eliminato");
				outToClient.writeBytes("colpito\n");
			}else{
				outToClient.writeBytes("mancato\n");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//handler per la bomba
	private void bomb() {
		
	}
	
	//funzione che esegue la mossa movimento
	private void move(Position pos) {
		int old_x = pos.getPos_x();
		int old_y = pos.getPos_y();
		int game_size = g.getSize_x();
		String choice = "";
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		boolean check = true;
		while(check){
			choice = "";
			System.out.println("Usa i seguenti tasti per spostarti: ");
			System.out.println("           nord");
			System.out.println("            [W]");
			System.out.println("  ovest [A]  +  [D] est");
			System.out.println("            [x]");
			System.out.println("            sud");
			while(!choice.equals("a") && !choice.equals("w") && !choice.equals("d") && !choice.equals("x")){
				try {
					System.out.println("scelta: ");
					choice = bufferedReader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			switch(choice){
				case "w":
					if(old_x-1>=0){
						System.out.println(old_x-1);
						pos.setPos_x(old_x-1);
						check = false;
					}else{
						System.out.println("[INFO] Mossa non consentita");
					}
					break;
				case "x":
					if(old_x+1<game_size){
						System.out.println(old_x+1);
						pos.setPos_x(old_x+1);
						check = false;
					}else{
						System.out.println("[INFO] Mossa non consentita");
					}
					break;
				case "a":
					if(old_y-1>=0){
						System.out.println(old_y-1);
						pos.setPos_y(old_y-1);
						check = false;
					}else{
						System.out.println("[INFO] Mossa non consentita");
					}
					break;
				case "d":
					if(old_y+1<game_size){
						System.out.println(old_y+1);
						pos.setPos_y(old_y+1);
						check = false;
					}else{
						System.out.println("[INFO] Mossa non consentita");
					}
					break;
			}
		}
		System.out.println(pos);
	}

	
	//handler per la cancellazione di un giocatore in partita
	private void playersUpdateDelete() {
		String response = "";
		StringReader reader;
		Player pl;
		String pl_name;
		try {
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
			pl = (Player) Player.unmarshallThat(reader);
			pl_name = pl.getName();
			g.removePlayer(pl_name);
			System.out.println("["+pl_name+"]");
		}catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	//handler per l'aggiunta di un giocatore
	private void playersUpdate() {
		String response = "";
		StringReader reader = null;
		Player pl;
		String pl_name;
		try {
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
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
				System.out.println("[INFO] Notifica nuovo giocatore!");
				g.addPlayer(pl);
				if(player.getMy_next().equals(pl.getMy_next()))
					player.setMy_next(pl_name);
				System.out.println("["+pl_name+"]");
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//handler per la gestione della lettura da standard input degli interi
	private static int integerReaderHandler(BufferedReader bufferedReader){
		int number = 0;
		boolean val = true;
		while(val){
			try {
				number = Integer.parseInt(bufferedReader.readLine());
				val = false;
			} catch (NumberFormatException e) {
				System.out.println("[INFO] Dato errato, riprova.");
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		return number;
		
	}

}
