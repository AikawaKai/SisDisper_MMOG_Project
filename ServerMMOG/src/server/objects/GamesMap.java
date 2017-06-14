package server.objects;

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
	
	//metodo per l'inserimento di una partita nella mappa delle partite
	public boolean put(String name, Game game){
		synchronized(games){ //sincronizzo per evitare che qualcuno scriva mentre aggiungo un nuovo riferimento alla mappa
			if(!games.containsKey(name)){
				games.put(name, game); 
				return true;
			}
		}
		return false;
			
	}
	
	//metodo per la restituzione del riferimento ad una partita, se questa esiste
	public Game get(String name){
		synchronized(games){ //blocco l'hashmap mentre la leggo
			return games.get(name); //restituisce il riferimento se la partita esista, null altrimenti
		}
	}
	
	//metodo che rimuove la partita dalla mappa dei giochi
	public boolean remove(String name){
		synchronized(games){ //sincronizzo per evitare che qualcuno scriva mentre rimuovo il riferimento
			if(games.remove(name)!=null)
				return true;
		}
		return false;
	}
	//metodo che aggiunge un giocatore ad una partita
	public int addPlayer(String game, Player pl) {  //non sincronizzo intero oggetto per massimizzare interleaving
		Game g;
		synchronized(games){ //sincronizzo per evitare che qualcuno scriva mentre leggo il riferimento
			g = games.get(game);
		}
		if(g!=null) //controllo che l'istanza esista
		{
			if(g.addPlayer(pl)) //metodo sincronizzato sull'istanza game
				return 1;//ho aggiunto il giocatore pl
			return 0; //giocatore con medesimo già presente
		}
		return -1; //la partita non esiste
	}
	
	//metodo che rimuove un giocatore dalla partita
	public int removePlayer(String game, String pl) { //non sincronizzo intero oggetto per massimizzare interleaving
		Game g;
		synchronized(games){ //sincronizzo per evitare che qualcuno scriva mentre leggo il riferimento
			g = games.get(game);
		}
		if(g!=null) //controllo che l'istanza esista
		{
			if(g.removePlayer(pl)) //metodo sincronizzato sull'istanza game
			{
				synchronized(g){ //sincronizzo per evitare che qualcuno stia aggiungendo giocatori mentre controllo
					if(g.numPlayers()==0) 
							this.remove(game); //metodo sincronizzato su games
				}
				return 1; //ho rimosso il giocatore "pl"
			}
			return 0; //il giocatore nella partita non esiste
		}
		return -1; //la partita non esiste
	}
	
	//metodo che fa una printa delle partite attualmente in corso
	public void gamesList() {
		int i = 1;
		System.out.println("Partite in corso:");
		synchronized(games){ //prendo possesso di tutta lista per la lettura
			for (Map.Entry<String, Game> entry : games.entrySet()) {
				System.out.println(i+": "+entry.getValue().getGame_name()); //getGame_name sincronizza sull'istanza game
				i++;
			}	
		}
	}
}
