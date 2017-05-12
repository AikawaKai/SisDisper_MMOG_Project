package server.objects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Position {
	private int pos_x;
	private int pos_y;
	
	public Position(int x, int y){
		pos_x = x;
		pos_y = y;
	}
	
	public int getX(){
		return pos_x;
	}
	
	public int getY(){
		return pos_y;
	}

}
