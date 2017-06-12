import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import peer.objects.Game;
import peer.objects.Player;

public class ThreadRequestsHandler extends Thread{
	
	private Game g;
	private Socket conn;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	
	public ThreadRequestsHandler(Socket connection, Game game){
		conn = connection;
		g = game;
	}
	
	public void run(){
		String response="";
		try {
            inFromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            outToClient = new DataOutputStream(conn.getOutputStream());
            response = inFromClient.readLine();
            requestsHandler(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

	private void requestsHandler(String response) {
		switch(response){
		case "newplayer":
			System.out.println("Notifica nuovo giocatore!");
			playersUpdate();
			break;
		case "deleteplayer":
			System.out.println("Notifica cancellazione giocatore!");
			playersUpdateDelete();
		default:
		}
		
	}

	private void playersUpdateDelete() {
		String response = "";
		JAXBContext jaxbContext;
		StringReader reader;
		Player pl;
		Unmarshaller unmarshaller;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			System.out.println(response);
			reader = new StringReader(response);
			pl = (Player) unmarshaller.unmarshal(reader);
			outToClient.writeBytes("ok\n");
			g.removePlayer(pl.getName());
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private void playersUpdate() {
		String response = "";
		JAXBContext jaxbContext;
		StringReader reader;
		Player pl;
		Unmarshaller unmarshaller;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			System.out.println(response);
			reader = new StringReader(response);
			pl = (Player) unmarshaller.unmarshal(reader);
			outToClient.writeBytes("ok\n");
			g.getPlayers().add(pl);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
