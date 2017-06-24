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

}
