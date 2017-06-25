import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import peer.objects.Bomb;
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
	private ArrayList<Player> playersToAdd;

	public ThreadSendRequestToPlayer(Player pl_i, String c, boolean []check_, Object res){
		player = SingletonFactory.getPlayerSingleton();
		player_i = pl_i;
		case_ = c;
		check = check_;
		result = res;
		playersToAdd = SingletonFactory.getPlayersToAdd();

	}

	public void run() {
		switch(case_){
		case "checkin":
			checkIn();
			break;
		case "newplayer":
			notifyNewPlayer();
			break;
		case "accepted":
			accept();
			break;
		case "notaccepted":
			notaccept();
			break;
		case "imin":
			notifyImIn();
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
		case "explosion":
			notifyExplosion();
			break;
		case "token":
			sendTokenToNext();
			break;
		case "victory":
			victory();
			break;
		}
	}

	// sto dicendo che voglio entrare
	private void checkIn() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("checkin CONTENT:"+player.marshallerThis()+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// provo ad aggiungermi come giocatore. Viene controllata la posizione
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
	
	// sono riuscito ad entrare, comunico al giocatore di avercela fatta (per poterlo sbloccare)
	private void notifyImIn() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("imin\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// metodo per segnalare che sto uscendo
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
				player.addOnePoint();
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
			outputStream.writeBytes("bomb CONTENT:"+((String) result)+"\n");
			inputStream.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	// notifica esplosione bomba
	private void notifyExplosion() {
		Bomb b = (Bomb) result;
		DataOutputStream outputStream = player_i.getSocketOutput();
		BufferedReader inputStream = player_i.getSocketInput();
		String status;
		String []status_next;
		try {
			outputStream.writeBytes("explosion CONTENT:"+b.getColor()+"\n");
			status = inputStream.readLine();
			status_next = status.split(" ");
			if(status_next[0].equals("colpito") && !player.isDead()){
				if(player.getMy_next().equals(player_i.getName()))
					player.setMy_next(status_next[1]);
				System.out.println("[INFO] Hai colpito il giocatore ["+player_i.getName()+"]");
				synchronized(b){
					if(b.getCounter()<3)
					{
						player.addOnePoint();
						b.setCounter(b.getCounter()+1);
					}	
				}
				
			}
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




	// metodo per segnalare che la posizione del giocatore è stata acettata da tutti
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
		synchronized(playersToAdd){
			for(int i=0; i<playersToAdd.size(); i++){
				if(playersToAdd.get(i).getName().equals(player_i.getName()))
				{
					playersToAdd.remove(i);
					break;
				}
			}
		}
	}
	
	
	// metodo per segnalare che la posizione non è stata accettata da tutti
	private void notaccept() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("notaccepted\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	// mando il token al mio next
	private void sendTokenToNext() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("token \n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
