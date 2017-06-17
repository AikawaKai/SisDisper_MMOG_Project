package server.objects;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Game {
	private int size_x;
	private int max_point;
	private int max_players;
	private String game_name;
	private HashSet<String>player_names = new HashSet<String>();
	private ArrayList<Player> players = new ArrayList<Player>(); //token ring
	
	public Game(){

	}
	
	public int getSize_x(){
		return size_x;
	}
	
	public int getMax_point(){
		return max_point;
	}
	
	public synchronized String getGame_name(){
		return game_name;
	}
	
	public void setGame_name(String name){
		game_name = name;
	}
	
	public void setSize_x(int x){
		size_x = x;
	}
	
	public void setMax_point(int points){
		max_point = points;
	}
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public void setPlayers(ArrayList<Player> pl){
		players = pl;
	}
	
	public HashSet<String> getPlayer_names(){
		return player_names;
	}
	
	public void setPlayer_names(HashSet<String> names){
		player_names = names;
	}
	
	public int getMax_Players(){
		return max_players;
	}
	
	public void setMax_Players(int max){
		max_players = max;
	}
	
	public synchronized Player getPlayer(String player_name){
		if(player_names.contains(player_name)){
			for(Player pl: players){
				if(pl.getName().equals(player_name)){
					return pl;
				}
			}
		}
		return null;
	}
	
	// metodo per l'aggiunta di un giocatore alla parita
	public synchronized boolean addPlayer(Player pl){
		if(player_names.contains(pl.getName()))
		{
			return false;
		}
		player_names.add(pl.getName());
		players.add(pl);
		return true;
	}
	
	// metodo per la rimozione di un giocatore di nome pl_name dalla partita
	public synchronized boolean removePlayer(String pl_name) {
		if(!player_names.contains(pl_name)){
			return false;
		}
		player_names.remove(pl_name);
		int i = 0;
		for(Player pl: players){
			if(pl.getName().equals(pl_name))
			{
				players.remove(i);
				break;
			}
			i++;
		}
		return true;		
	}

	// metodo toString della partita
	public synchronized String toString(){
		String players_string = "[Players]\n";
		String player_str = "";
		int i = 1;
		for (Player pl : players) {
			player_str = i+")Player: "+ pl.getName();
			players_string = players_string + player_str + "\n";
			i++;
		}
		return "Game name: "+game_name+"\n"+"Size: "+size_x+"\nMax_point: "+max_point+"\n"+players_string;
	}
	
	// metodo per il conteggio dei giocatori attualmente presenti in parita
	public synchronized int numPlayers() {
		return players.size();
	}

	public synchronized Player getFirstPlayer() {
		if(players.size()>0){
			return players.get(0);
		}
		return null;
	}
	
	public synchronized String getPosOnGameArea(Position p){
		String[][] matrix = new String[size_x][size_x];
		for(int i=0;i<size_x;i++){
			for(int j=0;j<size_x;j++){
				if(i<size_x/2 && j<size_x/2)
					matrix[i][j] = "b";
				else if(i>=size_x/2 && j<size_x/2)
					matrix[i][j] = "g";
				else if(i<size_x/2 && j>=size_x/2)
					matrix[i][j] = "y";
				else
					matrix[i][j] = "r";
			}
		}
		matrix[p.getPos_x()][p.getPos_y()] = "X";
		return matrixToString(matrix);
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	public String matrixToString(String[][] matrix){
		String lineSeparator = System.lineSeparator();
		StringBuilder sb = new StringBuilder();

		for (String[] row : matrix) {
			String string_row = "";
			for(String el: row){
				string_row = string_row+" "+el+" ";
			}
		    sb.append(string_row)
		      .append(lineSeparator);
		}
		return sb.toString();
	}

	public synchronized void forwardToken(String current_player) {
		int num_players = players.size();
		Player toForward = null;
		for(int i=0; i<num_players;i++){
			if(players.get(i).getName().equals(current_player)){
				if(i<num_players-1)
					toForward = players.get(i+1);
				else
					toForward = players.get(0);
				break;
			}
		}
		toForward.sendMessage("token\n");
		
	}

	public synchronized void sendNewPos(Player pl) {
		for(Player pl_i: players){
			if(!pl_i.getName().equals(pl.getName()))
				sendPosToPlayer(pl_i, pl.getPos());
		}
	}

	private void sendPosToPlayer(Player pl_i, Position pos) {
		Socket peerSocket = null;
		String response = "";
		String position = null;
		DataOutputStream outToPeer = null;
		BufferedReader inFromPeer = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Position.class);
			position = asString(jaxbContext, pos);
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}
		try {
			peerSocket = new Socket(pl_i.getIp(), pl_i.getPort());
			outToPeer = new DataOutputStream(peerSocket.getOutputStream());
			inFromPeer = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
			outToPeer.writeBytes("newpos\n");
			response = inFromPeer.readLine();
			outToPeer.writeBytes(position+"\n");
			response = inFromPeer.readLine();
			if(response.equals("colpito"))
			{
				System.out.println("Hai colpito il giocatore ["+pl_i.getName()+"]");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String asString(JAXBContext pContext, Object pObject) throws JAXBException {
		StringWriter sw = new StringWriter();
		Marshaller marshaller = pContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.marshal(pObject, sw);
		return sw.toString();
	}

	public synchronized Position genRandPosition() {
		int x = randInt(0, size_x-1);
		int y = randInt(0, size_x-1);
		Position pos =  new Position();
		pos.setPos_x(x);
		pos.setPos_y(y);
		return pos;
	}
	
}


