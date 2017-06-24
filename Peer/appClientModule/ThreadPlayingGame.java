import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.SingletonFactory;

// ------------------------------------------------------------------------------------ 
//                                  [PEER SERVER]                                        
//
// Accetta le richieste e crea per ogni richiesta un nuovo threadrequesthandler.        
// All'inizio verifica la posizione in cui inserire il giocatore, finché non ne trova   
// una libera (distribuito)                                                             
// -------------------------------------------------------------------------------------

public class ThreadPlayingGame extends Thread {
	private Game game;
	private ServerSocket ws;
	private Socket connectionSocket;
	private WebTarget target;
	private boolean first;
	
	public ThreadPlayingGame(ServerSocket welcomeSocket, boolean f){
		first = f;
		game = SingletonFactory.getGameSingleton();
		ws = welcomeSocket;
		target = SingletonFactory.getWebTargetSingleton();
		
	}
	
	public void run(){
		Player player = SingletonFactory.getPlayerSingleton();
		
		// se sono il primo mi mando il token da solo
		if(first){
			try {
				player.setPos(game.genRandPosition());
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, "token", new boolean[1], new Object());
				pl_hl.start();
				pl_hl.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}else{
			//altrimenti provo ad entrare
			//comeInNewPlayer();
			sendRequestToAll("checkin", new boolean[1], player);
		}
		ThreadSensorHandler sensorHl = new ThreadSensorHandler(0.7, 37);
		ThreadBufferMovesWriter bufferWriter = new ThreadBufferMovesWriter();
		sensorHl.start();
		bufferWriter.start();
		System.out.println("Partita "+game.getGame_name()+" in corso...");
		System.out.println("Usa i seguenti tasti per spostarti: ");
		System.out.println("           nord");
		System.out.println("            [W]");
		System.out.println("  ovest [A]  +  [D] est");
		System.out.println("            [x]");
		System.out.println("            sud");
		System.out.println("Bombe [Q]");
		while(true && !player.isDead()){
			try {
				connectionSocket = ws.accept();
				ThreadRequestsHandler clientHandler = new ThreadRequestsHandler(target, connectionSocket);
				clientHandler.start();
			} catch (IOException e) {
				break;
			}
		}
	}

	
	
	//manda la richiesta a tutti eccetto me stesso
	private void sendRequestToAll(String request, boolean[] check, Object objectToSend) {
		String player_name = SingletonFactory.getPlayerSingleton().getName();
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		for(Player pl_i: game.getPlayers()){
			if(!pl_i.getName().equals(player_name))
			{
				
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, request, check, objectToSend);
				threads.add(pl_hl);
				pl_hl.start();
			}
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
