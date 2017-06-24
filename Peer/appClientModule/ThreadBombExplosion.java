import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

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
			System.out.println("[INFO] Sei morto a causa della tua stessa bomba!");
			player.killPlayer();
			sendRequestDeletePlayer();
		}
		sendRequestToAll("explosion", new boolean[1], color);
		WebTarget target = SingletonFactory.getWebTargetSingleton();
		if(!player.isDead() && player.getPoints()>=game.getMax_point())
		{
			target.path("deletegame").path(game.getGame_name()).request().delete();
			sendRequestToAll("victory", new boolean[1], new Object());
			System.out.println("[INFO] Hai vinto!");
			player.killPlayer();
		}
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

	//funzione per mandare la richiesta di cancellazione al server e agli altri peer
	private void sendRequestDeletePlayer() {
		WebTarget target = SingletonFactory.getWebTargetSingleton();
		Game game = SingletonFactory.getGameSingleton();
		Player player = SingletonFactory.getPlayerSingleton();
		target.path("deleteplayer").path(game.getGame_name()).path(player.getName()).request().delete();
		sendRequestToAll("deleteplayer", new boolean[1], new Object());
	}

}
