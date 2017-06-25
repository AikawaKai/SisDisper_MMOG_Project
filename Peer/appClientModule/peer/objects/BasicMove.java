package peer.objects;

public class BasicMove extends Move {
	private String direction;

	public BasicMove(String m){
		direction = m;
	}

	public String getMovement() {
		return direction;
	}

	public String toString(){
		return direction;
	}

	//funzione che esegue la mossa movimento
	public void move(Player player, Game game) {
		Position pos = player.getPos();
		int old_x = pos.getPos_x();
		int old_y = pos.getPos_y();
		int game_size = game.getSize_x();
		switch(direction){
		case "w":
			if(old_x-1>=0){
				pos.setPos_x(old_x-1);
				System.out.println("[INFO] Ti sei spostato");
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		case "x":
			if(old_x+1<game_size){
				pos.setPos_x(old_x+1);
				System.out.println("[INFO] Ti sei spostato");
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		case "a":
			if(old_y-1>=0){
				pos.setPos_y(old_y-1);
				System.out.println("[INFO] Ti sei spostato");
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		case "d":
			if(old_y+1<game_size){
				System.out.println("[INFO] Ti sei spostato");
				pos.setPos_y(old_y+1);
			}else{
				System.out.println("[INFO] Mossa non consentita");
			}
			break;
		}
	}

}
