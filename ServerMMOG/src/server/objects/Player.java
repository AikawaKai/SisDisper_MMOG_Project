package server.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private Position pos;
	private String name;
	private int points;
	private Game game;
	
	public Player(String n, int x, int y, Game g){
		name = n;
		pos = new Position(x, y);
		game = g;
		points = 0;
	}
	
	public String getName(){
		return name;
	}
	
	public Position getPos(){
		return pos;
	}
	
	public Game getGame(){
		return game;
	}
	
	public int getPoints(){
		return points;
	}

}
