import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import peer.objects.Player;
import peer.objects.SingletonFactory;

//------------------------------------------------------------------------------------ 
//                               [PEER CLIENT THREAD]                                        
//
//          Thread handler per mandare le diverse richieste agli altri peer   
//-------------------------------------------------------------------------------------
public class ThreadSendRequestToPlayer extends Thread {
	private Player player;
	private Player player_i;
	private String case_;
	private boolean []check;
	private Object result;

	public ThreadSendRequestToPlayer(Player pl_i, String c, boolean []check_, Object res){
		player = SingletonFactory.getPlayerSingleton();
		player_i = pl_i;
		case_ = c;
		check = check_;
		result = res;

	}

	public void run() {
		switch(case_){
		case "newplayer":
			notifyNewPlayer();
			break;
		case "deleteplayer":
			notifyDeletePlayer();
			break;
		case "sendnewpos":
			sendNewPos();
			break;
		case "bomb":
			notifyBomb();
			break;
		case "token":
			sendTokenToNext();
			break;
		case "accepted":
			accept();
			break;
		case "notaccepted":
			notaccept();
			break;
		case "victory":
			victory();
			break;
		}
	}

	//metodo per segnalare l'arrivo del nuovo giocatore
	@SuppressWarnings("unchecked")
	private void notifyNewPlayer() {
		BufferedReader inputStream = player_i.getSocketInput();
		DataOutputStream outputStream = player_i.getSocketOutput();
		String response = "";
		try {
			outputStream.writeBytes("newplayer CONTENT:"+player.marshallerThis()+"\n");
			response = inputStream.readLine();
			if(response.equals("ko"))
			{
				synchronized(check){
					check[0] = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			synchronized(check){
				check[0] = true;
				synchronized(result){
					((ArrayList<Player>) result).add(player_i);
				}
			}
		}

	}

	// metodo per segnalare la cancellazione del giocatore
	private void notifyDeletePlayer() {
		DataOutputStream outToPeer = player_i.getSocketOutput();
		BufferedReader inFromPeer = player_i.getSocketInput();
		try {
			outToPeer.writeBytes("deleteplayer CONTENT:"+player.marshallerThis()+"\n");
			inFromPeer.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// metodo per segnalare la nuova posizione dopo uno spostamento
	private void sendNewPos() {
		DataOutputStream outToPeer = player_i.getSocketOutput();
		BufferedReader inFromPeer = player_i.getSocketInput();
		String response = "";
		String []status_next;
		try {
			outToPeer.writeBytes("newpos CONTENT:"+player.getPos().marshallerThis()+"\n");
			response = inFromPeer.readLine();
			status_next = response.split(" ");
			if(status_next[0].equals("colpito"))
			{
				if(player.getMy_next().equals(player_i.getName()))
					player.setMy_next(status_next[1]);
				System.out.println("[INFO] Hai colpito il giocatore ["+player_i.getName()+"]");
				player.setPoints(player.getPoints()+1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// metodo per segnalare della bomba lanciata
	private void notifyBomb() {
		BufferedReader inputStream = player_i.getSocketInput();
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			System.out.println("Stai lanciando la bomba verso "+player_i.getName());
			outputStream.writeBytes("bomb CONTENT:"+((String) result)+"\n");
			inputStream.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}

	}
	
	
	// metodo per segnalare la propria vittoria
	private void victory() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("victory \n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}





	private void accept() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		BufferedReader inFromPeer = player_i.getSocketInput();
		String response = "";
		try {
			outputStream.writeBytes("accepted\n");
			outputStream.writeBytes(((Player)result).getName()+"\n");
			response = inFromPeer.readLine();
			Player my_prev = Player.unmarshallThat(new StringReader(response));
			if(((Player)result).equals(player_i))
			{
				player.setMy_next(my_prev.getMy_next());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void notaccept() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("notaccepted\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendTokenToNext() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("token \n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
