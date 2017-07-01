package server.objects;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Player {
	private String name;
	private String ip;
	private int port;
	private Position pos;
	private String my_next = null;
	private Socket socket=null;
	private BufferedReader inputStream = null;
	private DataOutputStream outputStream = null;
	private int points;
	private boolean is_dead=false;
	private int activebombs = 0;


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

	public synchronized void setPort(int p){
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

	public synchronized String getMy_next(){
		return my_next;
	}

	public synchronized void setMy_next(String next){
		my_next = next;
	}

	public synchronized void setPoints(int p){
		points = p;
	}

	public synchronized int getPoints(){
		return points;
	}

	public synchronized void addOnePoint(){
		points = points + 1;
	}
	
	public synchronized void addActiveBomb(){
		activebombs++;
	}
	
	public synchronized void removeActiveBomb() {
		activebombs--;
	}
	
	public synchronized int getActiveBombs(){
		return activebombs;
	}

	public synchronized void killPlayer(){
		is_dead = true;
	}

	public synchronized boolean isDead(){
		return is_dead;
	}

	public synchronized boolean equals(Player pl){
		if(pl.getName().equals(name))
		{
			if(pl.getIp().equals(this.getIp()) && pl.getPort() == this.getPort())
				return true;
		}
		return false;
	}

	public synchronized BufferedReader  getSocketInput(){
		if(socket==null)
			startSocket();
		return inputStream;
	}

	public synchronized DataOutputStream getSocketOutput(){
		if(socket==null)
			startSocket();
		return outputStream;
	}

	private synchronized void startSocket() {
		try {
			socket = new Socket(ip, port);
			outputStream = new DataOutputStream(socket.getOutputStream());
			inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {

		}
	}

	public synchronized void closeSocket(){
		if(socket!=null){
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void sendMessage(String message) {
		if(socket==null){
			startSocket();
		}
		try {
			DataOutputStream outToPeer = new DataOutputStream(socket.getOutputStream());
			outToPeer.writeBytes(message);
		} catch (IOException e) {

		}
	}

	public synchronized String getMessage(){
		if(socket==null){
			startSocket();
		}
		String response = null;
		try {
			response = inputStream.readLine();
		} catch (IOException e) {

		}
		return response;
	}

	public synchronized String marshallerThis(){
		String player_s = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Player.class);
			StringWriter sw = new StringWriter();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(this, sw);
			return sw.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return player_s;
	}

	public static Player unmarshallThat(StringReader player_s){
		Player gen_play = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Player.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			gen_play = (Player) unmarshaller.unmarshal(player_s);
			return gen_play;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return gen_play;
	}

	public synchronized boolean isInArea(Position[] area) {
		Position a = area[0];
		Position b = area[1];
		int my_x = pos.getPos_x();
		int my_y = pos.getPos_y();
		if (my_x>=a.getPos_x() && my_x<=b.getPos_x() && my_y>=a.getPos_y() && my_y<=b.getPos_y())
			return true;
		return false;
	}


}