package server.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Position {
	private int pos_x;
	private int pos_y;
	
	public Position(){
	}
	
	public int getPos_x(){
		return pos_x;
	}
	
	public void setPos_x(int x){
		pos_x = x;
	}
	
	public int getPos_y(){
		return pos_y;
	}
	
	public void setPos_y(int y){
		pos_y = y;
	}
	
	public void changePos(int x, int y){
		pos_x = x;
		pos_y = y;
	}

	public boolean samePos(Position position) {
		if (pos_x == position.getPos_x() && pos_y == position.getPos_y()){
			return true;
		}
		return false;
	}

}
