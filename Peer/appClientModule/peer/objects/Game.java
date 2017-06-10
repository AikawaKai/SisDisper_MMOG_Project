package peer.objects;


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
	
	public ArrayList<Player> getPlayers(){
		return players;
	}
	
	public void setPlayers(ArrayList<Player> pl){
		players = pl;
	}
	
	public HashSet<String> getPlayer_Names(){
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
	
	public synchronized boolean insertPlayer(Player pl){
		if(player_names.contains(pl.getName()))
		{
			return false;
		}
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		player_names.add(pl.getName());
		players.add(pl);
		return true;
	}
	
	public synchronized boolean removePlayer(String pl_name) {
		if(player_names.contains(pl_name)){
			/*
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
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
		return false;
	}
	
	public synchronized String toString(){
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		String players_string = "";
		String player_str = "";
		int i = 1;
		for (Player pl : players) {
			player_str = "Player"+i+": "+ pl.getName();
		    players_string = players_string + player_str + "\n";
		    i++;
		}
		return "Size: "+size_x+"\nMax_point: "+max_point+"\nName: "+game_name+"\n"+players_string;
	}
	
}


