package server.objects;


import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Game {
	private int size_x;
	private int max_point;
	private int max_players;
	private String game_name;
	private HashSet<String>player_names = new HashSet<String>();
	private ArrayList<Player> players = new ArrayList<Player>(); //token ring
	private ArrayList<ArrayList<Player>> toAdd = new ArrayList<ArrayList<Player>>();
	private ArrayList<DeletePlayer> toDelete = new ArrayList<DeletePlayer>();
	
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
	
	// metodo per l'aggiunta di un giocatore alla parita
	public synchronized boolean addPlayer(Player pl){
		if(player_names.contains(pl.getName()))
		{
			return false;
		}
		player_names.add(pl.getName());
		ArrayList<Player> list;
		players.add(pl);
		list = new ArrayList<Player>(players);
		synchronized(toAdd){ //sincronizzazione per la notify al thread che si occupa di aggiornare i giocatori
			toAdd.add(list);
			toAdd.notify();
		}
		return true;
	}
	
	// metodo per la rimozione di un giocatore di nome pl_name dalla partita
	public synchronized boolean removePlayer(String pl_name) {
		if(!player_names.contains(pl_name)){
			return false;
		}
		player_names.remove(pl_name);
		DeletePlayer dp = null;
		ArrayList<Player> list;
		Player player_to_delete;
		int i = 0;
		for(Player pl: players){
			if(pl.getName().equals(pl_name))
			{
				player_to_delete = players.remove(i);
				list = new ArrayList<Player>(players);
				dp = new DeletePlayer(list, player_to_delete);
				break;
			}
			i++;
		}
		synchronized(toDelete){ //sincronizzazione per la notify al thread che si occupa di cancellare
			toDelete.add(dp);
			toDelete.notify();
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
	
	// metodo che restituisce il rif. alla lista di giocatori da aggiungere
	public synchronized ArrayList<ArrayList<Player>> getToAddList() {
		return toAdd;
	}
	
	// metodo che restituisce il rif. alla lista di giocatori da cancellare
	public synchronized ArrayList<DeletePlayer> getToDelList() {
		return toDelete;
	}
	
	// metodo per il conteggio dei giocatori attualmente presenti in parita
	public synchronized int numPlayers() {
		return players.size();
	}
	
}


