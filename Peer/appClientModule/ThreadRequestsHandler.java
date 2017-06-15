import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import peer.objects.Game;
import peer.objects.Player;
import peer.objects.Position;

public class ThreadRequestsHandler extends Thread{
	
	private Game g;
	private String player_name;
	private Socket conn;
	private BufferedReader inFromClient;
	private DataOutputStream outToClient;
	
	public ThreadRequestsHandler(Socket connection, String my_name, Game game){
		conn = connection;
		g = game;
		player_name = my_name;
	}
	
	public void run(){
		String response="";
		try {
            inFromClient = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            outToClient = new DataOutputStream(conn.getOutputStream());
            response = inFromClient.readLine();
            requestsHandler(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

	private void requestsHandler(String response) {
		switch(response){
		case "newplayer":
			System.out.println("Notifica nuovo giocatore!");
			playersUpdate();
			break;
		case "deleteplayer":
			System.out.println("Notifica cancellazione giocatore!");
			playersUpdateDelete();
		case "token":
			System.out.println("È il tuo turno! Fai una mossa!");
			doMove();
		default:
		}
	}

	private void doMove() {
		Player pl = g.getPlayer(player_name);
		System.out.println(pl.getPos());
		System.out.println(g.getPosOnGameArea(pl.getPos()));
		int scelta=0;
		while(scelta!=1 && scelta!=2){
			System.out.println("Scegli cosa fare:");
			System.out.println("1 - muoviti in una direzione");
			System.out.println("2 - Usa una bomba (se ne possiedi una)");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Scelta: ");
			scelta = integerReaderHandler(bufferedReader);
		}
		switch(scelta){
			case 1:
				move(pl.getPos());
				System.out.println("Spostato:");
				System.out.println(g.getPosOnGameArea(pl.getPos()));
				g.forwardToken(player_name);
			case 2:
				bomb();
			default:
				break;
		}
		System.out.println("Fine turno.");
	}
	
	private void bomb() {
		
	}

	private void move(Position pos) {
		int old_x = pos.getPos_x();
		int old_y = pos.getPos_y();
		int game_size = g.getSize_x();
		String choice = "";
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		boolean check = true;
		while(check){
			choice = "";
			System.out.println("Usa i seguenti tasti per spostarti: ");
			System.out.println("           nord");
			System.out.println("            [W]");
			System.out.println("  ovest [A]  +  [D] est");
			System.out.println("            [x]");
			System.out.println("            sud");
			while(!choice.equals("a") && !choice.equals("w") && !choice.equals("d") && !choice.equals("x")){
				try {
					System.out.println("scelta: ");
					choice = bufferedReader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			switch(choice){
				case "w":
					if(old_x-1>=0){
						System.out.println(old_x-1);
						pos.setPos_x(old_x-1);
						check = false;
					}else{
						System.out.println("Mossa non consentita");
					}
					break;
				case "x":
					if(old_x+1<game_size){
						System.out.println(old_x+1);
						pos.setPos_x(old_x+1);
						check = false;
					}else{
						System.out.println("Mossa non consentita");
					}
					break;
				case "a":
					if(old_y-1>=0){
						System.out.println(old_y-1);
						pos.setPos_y(old_y-1);
						check = false;
					}else{
						System.out.println("Mossa non consentita");
					}
					break;
				case "d":
					if(old_y+1<game_size){
						System.out.println(old_y+1);
						pos.setPos_y(old_y+1);
						check = false;
					}else{
						System.out.println("Mossa non consentita");
					}
					break;
			}
		}
		System.out.println(pos);
		
	}


	private void playersUpdateDelete() {
		String response = "";
		JAXBContext jaxbContext;
		StringReader reader;
		Player pl;
		String pl_name;
		Unmarshaller unmarshaller;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
			pl = (Player) unmarshaller.unmarshal(reader);
			outToClient.writeBytes("ok\n");
			pl_name = pl.getName();
			g.removePlayer(pl_name);
			System.out.println("["+pl_name+"]");
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private void playersUpdate() {
		String response = "";
		JAXBContext jaxbContext;
		StringReader reader;
		Player pl;
		String pl_name;
		Unmarshaller unmarshaller;
		try {
			jaxbContext = JAXBContext.newInstance(Player.class);
			unmarshaller = jaxbContext.createUnmarshaller();
			outToClient.writeBytes("ack\n");
			response = inFromClient.readLine();
			reader = new StringReader(response);
			pl = (Player) unmarshaller.unmarshal(reader);
			outToClient.writeBytes("ok\n");
			pl_name = pl.getName();
			g.addPlayer(pl); // metodo sincronizzato
			System.out.println("["+pl_name+"]");
		} catch (JAXBException e1) {
			e1.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static int integerReaderHandler(BufferedReader bufferedReader){
		int number = 0;
		boolean val = true;
		while(val){
			try {
				number = Integer.parseInt(bufferedReader.readLine());
				val = false;
			} catch (NumberFormatException e) {
				System.out.println("Dato errato.");
				System.out.print("Riprova: ");
			} catch (IOException e){
				e.printStackTrace();
			}
		}
		return number;
		
	}

}
