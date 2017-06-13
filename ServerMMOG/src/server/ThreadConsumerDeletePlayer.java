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

import server.objects.DeletePlayer;
import server.objects.Player;

public class ThreadConsumerDeletePlayer extends Thread{
	private ArrayList<DeletePlayer> players_to_delete;

	public ThreadConsumerDeletePlayer(ArrayList<DeletePlayer> pls){
		players_to_delete = pls;
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
		Player to_delete;
		DeletePlayer current;
		ArrayList<Player> to_notify;
		while(true){
			synchronized(players_to_delete){
				if(players_to_delete.size()==0){
					try {
						players_to_delete.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				current = players_to_delete.remove(0);
				to_notify = current.getArrayList();
				to_delete = current.getPlayer();
				lenght = to_notify.size();
				try {
					player_s = asString(jaxbContext, to_delete);
				} catch (JAXBException e1) {
					e1.printStackTrace();
				}
				for(int i=0; i<lenght;i++){
					while(!response.equals("ok"))
					{
						try {
							Player pl_i = to_notify.get(i);
							System.out.println("dati"+pl_i.getName()+pl_i.getPort());
							peerSocket = new Socket(pl_i.getIp(), pl_i.getPort());
							outToPeer = new DataOutputStream(peerSocket.getOutputStream());
							inFromPeer = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
							outToPeer.writeBytes("deleteplayer\n");
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
