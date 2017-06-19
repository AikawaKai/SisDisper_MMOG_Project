import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.Position;

// ------------------------------------------------------------------------------------ 
//                                  [PEER SERVER]                                        
//
// Accetta le richieste e crea per ogni richiesta un nuovo threadrequesthandler.        
// All'inizio verifica la posizione in cui inserire il giocatore, finché non ne trova   
// una libera (distribuito)                                                             
// -------------------------------------------------------------------------------------

public class ThreadPlayingGame extends Thread {
	private Game g;
	private String player_name;
	private ServerSocket ws;
	private Socket connectionSocket;
	private Player player;
	private WebTarget target;
	
	public ThreadPlayingGame(WebTarget target_, String my_name, Game game, ServerSocket welcomeSocket){
		g = game;
		ws = welcomeSocket;
		player_name = my_name;
		player = g.getPlayer(player_name);
		target = target_;
		
	}
	
	public void run(){
		comeInNewPlayer();
		System.out.println("Partita "+g.getGame_name()+" in corso...");
		while(true){
			try {
				connectionSocket = ws.accept();
				ThreadRequestsHandler clientHandler = new ThreadRequestsHandler(target, connectionSocket, player_name, g);
				clientHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	//funzione che si occupa di generare una nuova posizione random per il nuovo giocatore
	//confermandola agli altri peer
	private void comeInNewPlayer() {
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		ArrayList<ThreadSendRequestToPlayer> threadsNotify = new ArrayList<ThreadSendRequestToPlayer>();
		boolean check[] = {true};
		ArrayList<Player> players_deleted = new ArrayList<Player>();
		while(check[0]){
			check[0] = false;
			Position pos = g.genRandPosition(); // questo forse conviene farlo dopo?
			player.setPos(pos);
			for(Player pl_i: g.getPlayers()){
				if(!pl_i.getName().equals(player_name))
				{
					ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "newplayer", check, players_deleted);
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
						ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "notaccepted", check, new Object());
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
			System.out.println("Sono bloccato qui?");
		}
		System.out.println("(HO SCELTO) Io sono "+player.getName()+" e ho scelto  "+player_next.getName());
		for(Player pl_i: g.getPlayers()){
			if(!pl_i.getName().equals(player_name))
			{
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "accepted", check, player_next);
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
