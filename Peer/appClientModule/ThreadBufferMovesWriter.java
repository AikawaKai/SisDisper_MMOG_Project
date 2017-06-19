import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ThreadBufferMovesWriter extends Thread {
	private BufferMoves moves;
	
	public ThreadBufferMovesWriter(BufferMoves m){
		moves = m;
	}
	
	public void run(){
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		String input ="";
		while(true){
			try {
				input = bufferedReader.readLine();
				if(checkInput(input))
				{
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
						synchronized(moves){
							moves.addMove(new Bomb("c"));
						}
						break;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean checkInput(String input) {
		if(input.equals("a") || input.equals("w") || input.equals("d") || input.equals("x") || input.equals("q")){
			return true;
		}
		return false;
	}

}
