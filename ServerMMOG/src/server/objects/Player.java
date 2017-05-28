package server.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private Position pos;
	private String name;
	private int points;
	private Game game;
	
	public Player(){
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String n){
		name = n;
	}
	
	public Position getPosition(){
		return pos;
	}
	
	public void setPosition(Position p){
		pos = p;
	}
	
	public Game getGame(){
		return game;
	}
	
	public void setGame(Game g){
		game = g;
	}
	
	public int getPoints(){
		return points;
	}
	
	public void setPoints(int pnt){
		points = pnt;
	}

}
