package peer.objects;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Game {
	private int size_x;
	private int max_point;
	private int max_players;
	private String game_name;
	private Map<String, Player> players = new HashMap<String, Player>();
	
	public Game(){

	}
	
	public int getSize_x(){
		return size_x;
	}
	
	public int getMax_point(){
		return max_point;
	}
	
	public String getGame_name(){
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
	
	public Map<String, Player> getPlayers(){
		return players;
	}
	
	public void setPlayers(Map<String, Player> pl){
		players = pl;
	}
	
	public int getMax_Players(){
		return max_players;
	}
	
	public void setMax_Players(int max){
		max_players = max;
	}
	
	public boolean insertPlayer(Player pl){
		if(players.containsKey(pl.getName()))
			return false;
		players.put(pl.getName(), pl);
		return true;
	}
	
	public boolean containsPlayer(String pl){
		if(players.containsKey(pl))
			return true;
		return false;
	}
	
	public void removePlayer(String pl) {
		players.remove(pl);
	}
	
	public String toString(){
		return "Size: "+size_x+"\nMax_point: "+max_point+"\nName: "+game_name;
	}

	
	
}


