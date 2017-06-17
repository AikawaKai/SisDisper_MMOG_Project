package server.objects;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private String name;
	private String ip;
	private int port;
	private Position pos;
	private Socket socket=null;
	private BufferedReader inputStream = null;
	private DataOutputStream outputStream = null;
	
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
	
	public synchronized BufferedReader  getSocketInput(){
		if(socket==null){
			startSocket();
		}
		return inputStream;
	}

	public synchronized DataOutputStream getSocketOutput(){
		if(socket==null){
			startSocket();
		}
		return outputStream;
	}
	
	private synchronized void startSocket() {
		try {
			socket = new Socket(ip, port);
			outputStream = new DataOutputStream(socket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private synchronized void closeSocket(){
		if(socket!=null){
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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