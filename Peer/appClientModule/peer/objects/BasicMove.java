package peer.objects;

public class BasicMove extends Move {
	private String direction;
	
	public BasicMove(String m){
		direction = m;
	}
	
	public String getMovement() {
		return direction;
	}
	
	public String toString(){
		return direction;
	}

}
