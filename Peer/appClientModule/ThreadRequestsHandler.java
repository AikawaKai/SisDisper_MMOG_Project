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
	private String player_name;
	private Socket conn;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	
	public ThreadRequestsHandler(Socket connection, String my_name, Game game){
		conn = connection;
		g = game;
		player_name = my_name;
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
		case "token":
			System.out.println("È il tuo turno! Fai una mossa!");
			doMove();
		default:
		}
		
	}

	private void doMove() {
		System.out.println("Scegli cosa fare:");
		System.out.println("1) muoviti in una direzione");
		System.out.println("2) Usa una bomba (se ne possiedi una)");
		
	}

	private void playersUpdateDelete() {
		String response = "";
		JAXBContext jaxbContext;
		StringReader reader;
		Player pl;
		String pl_name;
		Unmarshaller unmarshaller;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
			pl = (Player) unmarshaller.unmarshal(reader);
			outToClient.writeBytes("ok\n");
			pl_name = pl.getName();
			g.removePlayer(pl_name);
			System.out.println("["+pl_name+"]");
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private void playersUpdate() {
		String response = "";
		JAXBContext jaxbContext;
		StringReader reader;
		Player pl;
		String pl_name;
		Unmarshaller unmarshaller;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
			pl = (Player) unmarshaller.unmarshal(reader);
			outToClient.writeBytes("ok\n");
			pl_name = pl.getName();
			g.addPlayer(pl); // metodo sincronizzato
			System.out.println("["+pl_name+"]");
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
