import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import peer.objects.Game;

public class ThreadPlayingGame extends Thread {
	private Game g;
	private ServerSocket ws;
	private Socket connectionSocket;
	
	public ThreadPlayingGame(Game game, ServerSocket welcomeSocket){
		g = game;
		ws = welcomeSocket;
		
	}
	
	public void run(){
		System.out.println("Partita "+g.getGame_name()+" in corso...");

		while(true){
			try {
				connectionSocket = ws.accept();
				ThreadRequestsHandler clientHandler = new ThreadRequestsHandler(connectionSocket, g);
				clientHandler.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

}
