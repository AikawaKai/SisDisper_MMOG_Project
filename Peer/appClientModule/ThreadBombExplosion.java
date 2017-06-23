import java.util.ArrayList;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.Position;
import peer.objects.SingletonFactory;

public class ThreadBombExplosion extends Thread{

	private String color;

	public ThreadBombExplosion(String c){
		color = c;
	}

	public void run(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.print("[INFO] Bomba "+color+" esplosa!\n");
		Player player = SingletonFactory.getPlayerSingleton();
		Game game = SingletonFactory.getGameSingleton();
		Position []area = game.getArea(color);
		if(player.isInArea(area)){
			player.killPlayer();
		}
		sendRequestToAll("explosion", new boolean[1], color);

	}

	//manda la richiesta a tutti eccetto me stesso
	void sendRequestToAll(String request, boolean[] check, Object objectToSend) {
		Game game = SingletonFactory.getGameSingleton();
		String player_name = SingletonFactory.getPlayerSingleton().getName();
		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
		for(Player pl_i: game.getPlayers()){
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
