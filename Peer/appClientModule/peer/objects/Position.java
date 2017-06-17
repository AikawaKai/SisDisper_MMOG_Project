package peer.objects;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
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

	public synchronized boolean equals(Position position) {
		if (pos_x == position.getPos_x() && pos_y == position.getPos_y()){
			return true;
		}
		return false;
	}
	
	public synchronized String toString(){
		return "\nPosizione [X: "+pos_x+" |Y: "+pos_y+"]\n";
	}
	
	public synchronized String marshallerThis(){
		String position = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Position.class);
			StringWriter sw = new StringWriter();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(this, sw);
			return sw.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return position;
	}
	
	public static Position unmarshallThat(StringReader position_s){
		Position gen_position = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Player.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			gen_position = (Position) unmarshaller.unmarshal(position_s);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return gen_position;
	}

}
