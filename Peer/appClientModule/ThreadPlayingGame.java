import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.Position;

// Server del Peer: Accetta le richieste e crea per ogni richiesta un nuovo threadrequesthandler
// All'inizio verifica la posizione in cui inserire il giocatore, finché non ne trova una libera (distribuito)
public class ThreadPlayingGame extends Thread {
	private Game g;
	private String player_name;
	private ServerSocket ws;
	private Socket connectionSocket;
	private Player player;
	
	public ThreadPlayingGame(String my_name, Game game, ServerSocket welcomeSocket){
		g = game;
		ws = welcomeSocket;
		player_name = my_name;
		player = g.getPlayer(player_name);
		
	}
	
	public void run(){
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		ArrayList<ThreadSendRequestToPlayer> threadsNotify = new ArrayList<ThreadSendRequestToPlayer>();
		boolean check[] = {true};
		while(check[0]){
			check[0] = false;
			Position pos = g.genRandPosition();
			player.setPos(pos);
			for(Player pl_i: g.getPlayers()){
				if(!pl_i.getName().equals(player_name))
				{
					ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "newplayer", check);
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
				for(Player pl_i: g.getPlayers()){
					if(!pl_i.getName().equals(player_name))
					{
						ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "notconfirmed", check);
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
		for(Player pl_i: g.getPlayers()){
			if(!pl_i.getName().equals(player_name))
			{
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "confirmed", check);
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
		System.out.println("Partita "+g.getGame_name()+" in corso...");
		while(true){
			try {
				connectionSocket = ws.accept();
				ThreadRequestsHandler clientHandler = new ThreadRequestsHandler(connectionSocket, player_name, g);
				clientHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
