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
	private boolean first;
	private Player player;

	public ThreadPlayingGame(ServerSocket welcomeSocket, boolean f){
		first = f;
		game = SingletonFactory.getGameSingleton();
		ws = welcomeSocket;
		player = SingletonFactory.getPlayerSingleton();

	}

	public void run(){
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
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			ArrayList<Boolean> checkin_status = new ArrayList<Boolean>();
			sendRequestToAll("checkin", new boolean[1], checkin_status);
			WebTarget target = SingletonFactory.getWebTargetSingleton();
			if(checkin_status.size()==0){
				System.out.println("[INFO] La partita è già finita. Mi spiace.");
				target.path("deletegame").path(game.getGame_name()).request().delete();
				System.exit(0);
			}
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
		if(first)
			System.out.println(game.getPosOnGameArea(player.getPos()));
		while(true){
			try {
				connectionSocket = ws.accept();
				ThreadRequestsHandler clientHandler = new ThreadRequestsHandler(connectionSocket);
				clientHandler.start();
			} catch (IOException e) {
				break;
			}
		}
	}

	//manda la richiesta a tutti eccetto me stesso
	void sendRequestToAll(String request, boolean[] check, Object objectToSend) {
		String player_name = SingletonFactory.getPlayerSingleton().getName();
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
}
