package peer.objects;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Bomb extends Move {
	private String color;
	private Player player;
	private int counter;
	
	public Bomb(){
		
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player pl) {
		this.player = pl;
	}
	
	public int getCounter(){
		return counter;
	}
	
	public void setCounter(int count){
		counter = count;
	}
	
	public String toString(){
		return color;
	}
	
	public synchronized String marshallerThis(){
		String bomb_s = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Bomb.class);
			StringWriter sw = new StringWriter();
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.marshal(this, sw);
			return sw.toString();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return bomb_s;
	}
	
	public static Bomb unmarshallThat(StringReader bomb_s){
		Bomb gen_bomb = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Bomb.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			gen_bomb = (Bomb) unmarshaller.unmarshal(bomb_s);
			return gen_bomb;
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		return gen_bomb;
	}

}
