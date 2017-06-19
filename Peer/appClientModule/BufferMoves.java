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
			return moves.get(0);
		return null;
	}

	public ArrayList<Move> getMoves() {
		return moves;
	}

	public void setMoves(ArrayList<Move> moves) {
		this.moves = moves;
	}

}
