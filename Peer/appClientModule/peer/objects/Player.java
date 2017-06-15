package peer.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private String name;
	private String ip;
	private int port;
	private Position pos;
	
	public Player(){
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String n){
		name = n;
	}
	
	public void setIp(String host){
		ip = host;
	}
	
	public synchronized String getIp(){
		return ip;
	}
	
	public void setPort(int p){
		port = p;
	}
	
	public synchronized int getPort(){
		return port;
	}
	
	public synchronized void setPos(Position p){
		pos = p;
	}
	
	public synchronized Position getPos(){
		return pos;
	}

}