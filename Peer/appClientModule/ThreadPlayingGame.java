import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.Position;
import peer.objects.SingletonFactory;

// ------------------------------------------------------------------------------------ 
//                                  [PEER SERVER]                                        
//
// Accetta le richieste e crea per ogni richiesta un nuovo threadrequesthandler.        
// All'inizio verifica la posizione in cui inserire il giocatore, finché non ne trova   
// una libera (distribuito)                                                             
// -------------------------------------------------------------------------------------

public class ThreadPlayingGame extends Thread {
	private Game g;
	private ServerSocket ws;
	private Socket connectionSocket;
	private WebTarget target;
	private boolean first;
	
	public ThreadPlayingGame(WebTarget target_, Game game, ServerSocket welcomeSocket, boolean f){
		first = f;
		g = game;
		ws = welcomeSocket;
		target = target_;
		
	}
	
	public void run(){
		comeInNewPlayer();
		if(first){
			try {
				Player player = SingletonFactory.getPlayerSingleton();
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, "token", new boolean[1], new Object());
				pl_hl.start();
				pl_hl.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ThreadSensorHandler sensorHl = new ThreadSensorHandler(0.7, 37);
		ThreadBufferMovesWriter bufferWriter = new ThreadBufferMovesWriter();
		sensorHl.start();
		bufferWriter.start();
		System.out.println("Partita "+g.getGame_name()+" in corso...");
		System.out.println("Usa i seguenti tasti per spostarti: ");
		System.out.println("           nord");
		System.out.println("            [W]");
		System.out.println("  ovest [A]  +  [D] est");
		System.out.println("            [x]");
		System.out.println("            sud");
		System.out.println("Bombe [Q]");
		while(true){
			try {
				connectionSocket = ws.accept();
				ThreadRequestsHandler clientHandler = new ThreadRequestsHandler(target, connectionSocket, g);
				clientHandler.start();
			} catch (IOException e) {
				break;
			}
		}
		
	}

	//funzione che si occupa di generare una nuova posizione random per il nuovo giocatore
	//confermandola agli altri peer
	private void comeInNewPlayer() {
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		ArrayList<ThreadSendRequestToPlayer> threadsNotify = new ArrayList<ThreadSendRequestToPlayer>();
		Player player = SingletonFactory.getPlayerSingleton();
		String player_name = player.getName();
		boolean check[] = {true};
		ArrayList<Player> players_deleted = new ArrayList<Player>();
		while(check[0]){
			check[0] = false;
			Position pos = g.genRandPosition();
			player.setPos(pos);
			for(Player pl_i: g.getPlayers()){
				if(!pl_i.getName().equals(player_name))
				{
					ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, "newplayer", check, players_deleted);
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
			threads = new ArrayList<ThreadSendRequestToPlayer>();
			if(check[0])
			{
				for(Player pl_to_del: players_deleted){
					g.removePlayer(pl_to_del.getName());
				}
				for(Player pl_i: g.getPlayers()){
					if(!pl_i.getName().equals(player_name))
					{
						ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, "notaccepted", check, new Object());
						threadsNotify.add(pl_hl);
						pl_hl.start();
					}
				}
				for(ThreadSendRequestToPlayer hl: threadsNotify){
					try {
						hl.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		threadsNotify = new ArrayList<ThreadSendRequestToPlayer>();
		Player player_next;
		int num_players = g.getPlayers().size();
		int choose = Main.randInt(0, num_players-1);
		player_next = g.getPlayers().get(choose);
		while(player_next.equals(player) && num_players>1){
			choose = Main.randInt(0, num_players-1);
			player_next = g.getPlayers().get(choose);
		}
		for(Player pl_i: g.getPlayers()){
			if(!pl_i.getName().equals(player_name))
			{
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, "accepted", check, player_next);
				threadsNotify.add(pl_hl);
				pl_hl.start();
			}
		}
		for(ThreadSendRequestToPlayer hl: threadsNotify){
			try {
				hl.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
