package peer.objects;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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
					matrix[i][j] = "v";
				else if(i<size_x/2 && j>=size_x/2)
					matrix[i][j] = "g";
				else
					matrix[i][j] = "r";
			}
		}
		matrix[p.getPos_x()][p.getPos_y()] = "[X]";
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
				if(el.contains("["))
					string_row = string_row+el;
				else
					string_row = string_row+" "+el+" ";
			}
		    sb.append(string_row)
		      .append(lineSeparator);
		}
		return sb.toString();
	}
	
	public synchronized boolean equals(Game other){
		int size = players.size();
		if(size!=other.getPlayers().size())
			return false;
		for(String name: player_names){
			if(!this.getPlayer(name).equals(other.getPlayer(name)))
				return false;
		}
		return true;
	}

	public synchronized Position genRandPosition() {
		int x = randInt(0, size_x-1);
		int y = randInt(0, size_x-1);
		Position pos =  new Position();
		pos.setPos_x(x);
		pos.setPos_y(y);
		return pos;
	}
	
	public synchronized String marshallerThis(){
		String game_s = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Game.class);
			StringWriter sw = new StringWriter();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(this, sw);
			return sw.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return game_s;
	}
	
	public static Game unmarshallThat(StringReader game_s){
		Game gen_game = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Game.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			gen_game = (Game) unmarshaller.unmarshal(game_s);
			return gen_game;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return gen_game;
	}

	public Position[] getArea(String color) {
		Position a = new Position();
		Position b = new Position();
		Position pos[] =  {a,b};
		switch(color){
			case "verde":
				a.setPos_x(0);
				a.setPos_y(0);
				b.setPos_x(size_x/2);
				b.setPos_y(size_x/2);
				return pos;
			case "rosso":
				a.setPos_x(size_x/2);
				a.setPos_y(0);
				b.setPos_x(size_x);
				b.setPos_y(size_x/2);
				return pos;
			case "blu":
				a.setPos_x(0);
				a.setPos_y(size_x/2);
				b.setPos_x(size_x/2);
				b.setPos_y(size_x);
				return pos;
			case "giallo":
				a.setPos_x(size_x/2);
				a.setPos_y(size_x/2);
				b.setPos_x(size_x);
				b.setPos_y(size_x);
				return pos;
		}
		return null;
	}
	
}


