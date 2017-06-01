package peer.objects;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GamesMap {
	HashMap <String, Game> games = new HashMap <String, Game>();
	
	public GamesMap(){
		
	}
	
	public void setGames(HashMap <String, Game> g){
		games = g;
	}
	
	public HashMap <String, Game> getGames(){
		return games;
	}
	
	public boolean containsKey(Object value){
		return games.containsKey(value);
	}
	
	public void put(String name, Game game){
		games.put(name, game);
	}
	
	public Game get(String name){
		return games.get(name);
	}
	
	public boolean remove(String name){
		if(games.remove(name)!=null){
			return true;
		}
		return false;
	}

	public void prettyPrint() {
		int i = 1;
		for (Map.Entry<String, Game> entry : games.entrySet()) {
			System.out.println("\n____________________\n");
		    System.out.println("nr: "+i);
		    System.out.println("Dettagli partita");
		    System.out.println(entry.getValue());
		    i++;
		}
		
	}

}
