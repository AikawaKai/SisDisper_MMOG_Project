package peer.objects;
import java.util.ArrayList;

public class BufferMoves {
	
	private ArrayList<Move> moves = new ArrayList<Move>();
	
	public BufferMoves(){
		
	}
	
	public synchronized void addMove(Move m){
		moves.add(m);
	}
	
	public  synchronized Move getFirst(){
		if(moves.size()>0)
			return moves.remove(0);
		return null;
	}

	public ArrayList<Move> getMoves() {
		return moves;
	}

	public void setMoves(ArrayList<Move> moves) {
		this.moves = moves;
	}

	public synchronized int size() {
		return moves.size();
	}
	
	public synchronized String toString(){
		String value = "";
		for(Move m: moves){
			value = value+"\n"+m.toString();
		}
		return value;
	}

}
