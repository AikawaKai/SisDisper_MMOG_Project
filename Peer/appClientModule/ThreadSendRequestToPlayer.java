import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import peer.objects.Player;

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
	
	public ThreadSendRequestToPlayer(Player pl, Player pl_i, String c, boolean []check_, Object res){
		player = pl;
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
		case "token":
			sendTokenToNext();
			break;
		case "accepted":
			accept();
			break;
		case "notaccepted":
			notaccept();
			break;
		}
	}

	private void notifyNewPlayer() {
		BufferedReader inputStream = player_i.getSocketInput();
		DataOutputStream outputStream = player_i.getSocketOutput();
		String response = "";
		try {
			outputStream.writeBytes("newplayer\n");
			response = inputStream.readLine();
			outputStream.writeBytes(player.marshallerThis()+"\n");
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

	private void notifyDeletePlayer() {
		DataOutputStream outToPeer = player_i.getSocketOutput();
		BufferedReader inFromPeer = player_i.getSocketInput();
			try {
				outToPeer.writeBytes("deleteplayer\n");
				inFromPeer.readLine();
				outToPeer.writeBytes(player.marshallerThis()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}

	private void sendNewPos() {
		DataOutputStream outToPeer = player_i.getSocketOutput();
		BufferedReader inFromPeer = player_i.getSocketInput();
		String response = "";
		String position = null;
		try {
			outToPeer.writeBytes("newpos\n");
			response = inFromPeer.readLine();
			position = player.getPos().marshallerThis();
			outToPeer.writeBytes(position+"\n");
			response = inFromPeer.readLine();
			if(response.equals("colpito"))
			{
				response = inFromPeer.readLine();
				if(player.getMy_next().equals(player_i.getName()))
					player.setMy_next(response);
				System.out.println("[INFO] Hai colpito il giocatore ["+player_i.getName()+"]");
			}else{
				response = inFromPeer.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Qualcosa è andato storto");
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
				System.out.println("Entro mai qui?");
				player.setMy_next(my_prev.getMy_next());
			}
			System.out.println("(Faccio richiesta) Io sono "+player.getName()+" e il mio next è "+player.getMy_next());
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
			outputStream.writeBytes("token\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
}
