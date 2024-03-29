package peer.objects;


import java.util.ArrayList;

import javax.ws.rs.client.WebTarget;

//------------------------------------------------------------------------------------ 
//							[SINGLETON FACTORY]                                        
//
//					  Factory per gli oggetti globali  
//-------------------------------------------------------------------------------------

public class SingletonFactory {
	static Player player;
	static Game game;
	static WebTarget target;
	static BufferMoves moves;
	static BufferMoves bombmoves;
	static ArrayList<Player> playersToAdd;
	static ArrayList<Bomb> explosions;
	
	public static void setPlayerSingleton(Player p){
		player = p;
	}
	
	public static Player getPlayerSingleton(){
		return player;
	}
	
	public static void setGameSingleton(Game g){
		game = g;
	}
	
	public static Game getGameSingleton(){
		return game;
	}
	
	public static void setWebTargetSingleton(WebTarget t){
		target = t;
	}
	
	public static WebTarget getWebTargetSingleton(){
		return target;
	}
	
	
	public static synchronized BufferMoves getSingletonMoves(){
			if(moves==null)
				moves = new BufferMoves();
			return moves;
	}
	
	public static synchronized BufferMoves getSingletonBombMoves(){
		if(bombmoves==null)
			bombmoves = new BufferMoves();
		return bombmoves;
	}
	
	public static synchronized ArrayList<Player> getPlayersToAdd(){
		if(playersToAdd==null)
			playersToAdd = new ArrayList<Player>();
		return playersToAdd;
	}

		
	public static synchronized ArrayList<Bomb> bombExploded(){
		if(explosions==null)
			explosions = new ArrayList<Bomb>();
		return explosions;
	}
	

}
