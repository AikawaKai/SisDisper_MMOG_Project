package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import server.objects.Game;
import server.objects.Player;

public class ThreadAddPlayerNotify extends Thread{
	private ArrayList<Player> players;
	private Player pl;
	private Game g;

	public ThreadAddPlayerNotify(ArrayList<Player> pls, Player added_pl, Game game){
		players = pls;
		pl = added_pl;
		g = game;
	}


	public void run(){
		Socket peerSocket = null;
		DataOutputStream outToPeer = null;
		BufferedReader inFromPeer = null;
		String response = "";
		JAXBContext jaxbContext;
		String player_s = null;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			player_s = asString(jaxbContext, pl);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		synchronized(g){
			for(Player pl_i: players){
				while(!response.equals("ok"))
				{
					try {
						peerSocket = new Socket(pl_i.getIp(), pl_i.getPort());
						outToPeer = new DataOutputStream(peerSocket.getOutputStream());
						inFromPeer = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
						outToPeer.writeBytes("newplayer\n");
						response = inFromPeer.readLine();
						if(response.equals("ack")){

							outToPeer.writeBytes(player_s+"\n");
						}
						response = inFromPeer.readLine();
						System.out.println(response);
					} catch (UnknownHostException e) {
						e.printStackTrace();
						System.out.println("Errore critico DNS");
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println("Errore nella lettura");
					}
				}

			}
		}


	}

	public String asString(JAXBContext pContext, Object pObject) throws JAXBException {

		StringWriter sw = new StringWriter();

		Marshaller marshaller = pContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.marshal(pObject, sw);

		return sw.toString();
	}

}
