package peer.objects;

//------------------------------------------------------------------------------------ 
//							[SINGLETON FACTORY]                                        
//
//					  Factory per gli oggetti globali  
//-------------------------------------------------------------------------------------

public class SingletonFactory {
	static Game game;
	static BufferMoves moves;
	static BufferMoves bombmoves;
	
	public static void setGameSingleton(Game g){
		game = g;
	}
	
	public static Game getGameSingleton(){
		return game;
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

}
