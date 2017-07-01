import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import peer.objects.BasicMove;
import peer.objects.Bomb;
import peer.objects.BufferMoves;
import peer.objects.Player;
import peer.objects.SingletonFactory;

public class ThreadBufferMovesWriter extends Thread {

	public ThreadBufferMovesWriter(){
	}

	public void run(){
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String input ="";
		BufferMoves moves = SingletonFactory.getSingletonMoves();
		BufferMoves bombs = SingletonFactory.getSingletonBombMoves();
		Player player = SingletonFactory.getPlayerSingleton();
		while(true){
			try {
				input = bufferedReader.readLine();
				switch(input){
				case "a":
				case "w":
				case "d":
				case "x":
					synchronized(moves){
						moves.addMove(new BasicMove(input));
					}
					break;
				case "q":
					Bomb b = null;
					synchronized(bombs){
						if(bombs.size()>0 && player.getActiveBombs()==0 )
							b = (Bomb) bombs.getFirst();
						else if(bombs.size()>0 && player.getActiveBombs()==1)
							System.out.println("[INFO] Hai già una bomba attiva!!");
					}
					synchronized(moves){
						moves.addMove(b);
					}
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
