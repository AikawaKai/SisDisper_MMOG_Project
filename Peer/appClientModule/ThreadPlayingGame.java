import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.Position;

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
		ArrayList<Boolean> checks = new ArrayList<Boolean>();
		boolean check = true;
		while(check){
			for(Player pl_i: g.getPlayers()){
				Position pos = g.genRandPosition();
				player.setPos(pos);
				if(!pl_i.getName().equals(player_name))
				{
					Boolean check_i = true;
					checks.add(check_i);
					ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(player, pl_i, "newplayer", check_i);
					pl_hl.start();
					threads.add(pl_hl);
				}
			}
			for(ThreadSendRequestToPlayer hl: threads){
				try {
					hl.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			check = false;
			for(Boolean res: checks){
				if(!res)
				{
					check = true;
					break;
				}
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
