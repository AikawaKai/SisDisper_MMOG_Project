package peer.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private Position pos;
	private String name;
	private int points;
	private String ip;
	private int port;
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
	
	public void setIp(String host){
		ip = host;
	}
	
	public String getIp(){
		return ip;
	}
	
	public void setPort(int p){
		port = p;
	}
	
	public int getPort(){
		return port;
	}

}