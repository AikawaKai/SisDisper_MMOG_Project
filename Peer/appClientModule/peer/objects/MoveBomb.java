package peer.objects;
public class MoveBomb extends Move {
	private Bomb bomb;
	
	public MoveBomb(Bomb b) {
		setBomb(b);
	}

	public Bomb getBomb() {
		return bomb;
	}

	public void setBomb(Bomb bomb) {
		this.bomb = bomb;
	}

	

	

}
