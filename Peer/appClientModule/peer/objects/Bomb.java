package peer.objects;

public class Bomb extends Move {
	private String color;
	
	public Bomb(String col){
		setColor(col);
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public String toString(){
		return color;
	}

}
