import javax.swing.text.Position;

public class MoveBase extends Move {
	private Position position;
	
	public MoveBase(Position pos){
		setPosition(pos);
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

}
