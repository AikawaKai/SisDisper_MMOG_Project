package peer.objects;
public class BombMove extends Move {
	private Bomb bomb;
	
	public BombMove(Bomb b) {
		setBomb(b);
	}

	public Bomb getBomb() {
		return bomb;
	}

	public void setBomb(Bomb bomb) {
		this.bomb = bomb;
	}

	

	

}
