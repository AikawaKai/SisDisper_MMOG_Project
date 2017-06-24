import java.util.ArrayList;

import peer.objects.Bomb;
import peer.objects.SingletonFactory;

public class ThreadBombExplosion extends Thread{

	private Bomb bomb;

	public ThreadBombExplosion(Bomb b){
		bomb = b;
	}

	public void run(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ArrayList<Bomb> explodedBombs = SingletonFactory.bombExploded();
		synchronized(explodedBombs){
			explodedBombs.add(bomb);
		}
	}

//	//manda la richiesta a tutti eccetto me stesso
//	void sendRequestToAll(String request, boolean[] check, Object objectToSend) {
//		Game game = SingletonFactory.getGameSingleton();
//		String player_name = SingletonFactory.getPlayerSingleton().getName();
//		ArrayList<ThreadSendRequestToPlayer> threads = new ArrayList<ThreadSendRequestToPlayer>();
//		for(Player pl_i: game.getPlayers()){
//			if(pl_i.getName().equals(player_name))
//				continue;
//			ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(pl_i, request, check, objectToSend);
//			threads.add(pl_hl);
//			pl_hl.start();
//		}
//		for(ThreadSendRequestToPlayer hl: threads){
//			try {
//				hl.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}

//	//funzione per mandare la richiesta di cancellazione al server e agli altri peer
//	private void sendRequestDeletePlayer() {
//		WebTarget target = SingletonFactory.getWebTargetSingleton();
//		Game game = SingletonFactory.getGameSingleton();
//		Player player = SingletonFactory.getPlayerSingleton();
//		target.path("deleteplayer").path(game.getGame_name()).path(player.getName()).request().delete();
//		sendRequestToAll("deleteplayer", new boolean[1], new Object());
//	}

}
