package server.objects;

import java.util.ArrayList;

public class DeletePlayer {
	private Player pl;
	private ArrayList<Player> pls;
	
	public DeletePlayer(ArrayList<Player> players, Player player){
		pls = players;
		pl = player;
	}
	
	public ArrayList<Player> getArrayList(){
		return pls;
	}
	
	public Player getPlayer(){
		return pl;
	}
}
