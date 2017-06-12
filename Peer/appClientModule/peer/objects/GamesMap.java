package peer.objects;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GamesMap {
	private HashMap <String, Game> games = new HashMap <String, Game>();
	
	public GamesMap(){
		
	}
	
	public void setGames(HashMap <String, Game> g){
		games = g;
	}
	
	public HashMap <String, Game> getGames(){
		return games;
	}
	
	public synchronized boolean put(String name, Game game){
		if(!games.containsKey(name)){
			/*
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
			games.put(name, game);
			return true;
		}
		return false;
			
	}
	
	public synchronized Game get(String name){
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		if(games.containsKey(name)){
			return games.get(name);
		}
		else
			return null;
	}
	
	public synchronized boolean remove(String name){
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		if(games.remove(name)!=null)
			return true;
		return false;
	}
	
	public synchronized int addPlayer(String game, Player pl) {
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		Game g = games.get(game);
		if(g!=null)
		{
			if(g.insertPlayer(pl))
			{
				return 1;
			}
			return 0;
		}
		return -1;
	}
	
	public synchronized int removePlayer(String game, String pl) {
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		Game g = games.get(game);
		if(g!=null)
		{
			if(g.removePlayer(pl))
			{
				return 1;
			}
			return 0;
		}
		return -1;
	}

	public synchronized void gamesList() {
		/*
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		int i = 1;
		System.out.println("Partite in corso:");
		for (Map.Entry<String, Game> entry : games.entrySet()) {
		    System.out.println(i+": "+entry.getValue().getGame_name());
		    i++;
		}	
	}
}
