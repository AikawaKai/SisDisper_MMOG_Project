package server.objects;

import java.util.HashMap;

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

}
