package server;

import java.util.ArrayList;


import server.objects.Player;

public class ThreadAddPlayerNotify extends Thread{
	private ArrayList<Player> players;
	private Player pl;
	
	public ThreadAddPlayerNotify(ArrayList<Player> pls, Player added_pl){
		players = pls;
		pl = added_pl;
	}
	
	public void run(){
		for(Player pl_i: players){
			pl_i.messageNewPlayer(pl);
		}
		
	}

}
