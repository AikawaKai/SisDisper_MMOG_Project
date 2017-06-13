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
	
	public boolean put(String name, Game game){
		synchronized(games){
			if(!games.containsKey(name)){
				games.put(name, game);
				return true;
			}
		}
		return false;
			
	}
	
	public Game get(String name){
		synchronized(games){
			if(games.containsKey(name))
				return games.get(name);
		}
		return null;
	}
	
	public boolean remove(String name){
		synchronized(games){
			if(games.remove(name)!=null)
				return true;
		}
		return false;
	}
	
	public int addPlayer(String game, Player pl) {  //non sincronizzo intero oggetto per massimizzare interleaving
		Game g;
		synchronized(games){
			g = games.get(game);
		}
		if(g!=null)
		{
			if(g.addPlayer(pl)) //metodo sincronizzato sull'istanza game
				return 1;
			return 0;
		}
		return -1;
	}
	
	public int removePlayer(String game, String pl) { //non sincronizzo intero oggetto per massimizzare interleaving
		Game g;
		synchronized(games){
			g = games.get(game);
		}
		if(g!=null)
		{
			if(g.removePlayer(pl)) //metodo sincronizzato sull'istanza game
				return 1;
			return 0;
		}
		return -1;
	}

	public synchronized void gamesList() {
		int i = 1;
		System.out.println("Partite in corso:");
		for (Map.Entry<String, Game> entry : games.entrySet()) {
			System.out.println(i+": "+entry.getValue().getGame_name()); //getGame_name sincronizza sull'istanza game
			i++;
		}	
	}
}
