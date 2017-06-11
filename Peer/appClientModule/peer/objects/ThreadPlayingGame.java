package peer.objects;

public class ThreadPlayingGame extends Thread {
	private Game g;
	
	public ThreadPlayingGame(Game game){
		g = game;
	}
	
	public void run(){
		System.out.println("Partita "+g.getGame_name()+" in corso...");
		while(true){
			
		}
	}

}
