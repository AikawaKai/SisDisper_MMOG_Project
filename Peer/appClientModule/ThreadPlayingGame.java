import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import peer.objects.Game;
import peer.objects.Player;

public class ThreadPlayingGame extends Thread {
	private Game g;
	private String player_name;
	private ServerSocket ws;
	private Socket connectionSocket;
	
	public ThreadPlayingGame(String my_name, Game game, ServerSocket welcomeSocket){
		g = game;
		ws = welcomeSocket;
		player_name = my_name;
		
	}
	
	public void run(){
		System.out.println("Partita "+g.getGame_name()+" in corso...");
		for(Player pl_i: g.getPlayers()){
			if(!pl_i.getName().equals(player_name))
			{
				ThreadSendRequestToPLayer pl_hl = new ThreadSendRequestToPLayer(g.getPlayer(player_name), pl_i, "newplayer");
				pl_hl.start();
			}
		}
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
