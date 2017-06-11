package server.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private String name;
	private String ip;
	private int port;
	
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