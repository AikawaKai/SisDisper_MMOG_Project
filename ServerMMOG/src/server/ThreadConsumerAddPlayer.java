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

import server.objects.Player;

public class ThreadConsumerAddPlayer extends Thread{
	private ArrayList<ArrayList<Player>> players_to_add;

	public ThreadConsumerAddPlayer(ArrayList<ArrayList<Player>> pls){
		players_to_add = pls;
	}


	public void run(){
		int lenght;
		Socket peerSocket = null;
		DataOutputStream outToPeer = null;
		BufferedReader inFromPeer = null;
		String response = "";
		JAXBContext jaxbContext = null;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
		} catch (JAXBException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String player_s = null;
		Player last;
		ArrayList<Player> current;
		while(true){
			synchronized(players_to_add){
				if(players_to_add.size()==0){
					try {
						players_to_add.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				current = players_to_add.remove(0);
				lenght = current.size();
				last = current.get(lenght-1);
				try {
					player_s = asString(jaxbContext, last);
				} catch (JAXBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				int i = 0;
				for(i=0; i<lenght-1;i++){
					while(!response.equals("ok"))
					{
						try {
							Player pl_i = current.get(i);
							System.out.println("dati"+pl_i.getName()+pl_i.getPort());
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
							peerSocket.close();
						} catch (UnknownHostException e) {
							e.printStackTrace();
							System.out.println("Errore critico DNS");
						} catch (IOException e) {
							e.printStackTrace();
							System.out.println("Errore nella lettura");
						}
						
					}
					response="";
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
