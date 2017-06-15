package peer.objects;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

	public void sendMessage(String message) {
		try {
			Socket peerSocket = new Socket(ip, port);
			DataOutputStream outToPeer = new DataOutputStream(peerSocket.getOutputStream());
			outToPeer.writeBytes(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}