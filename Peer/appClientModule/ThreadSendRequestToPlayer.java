import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

import peer.objects.Player;

public class ThreadSendRequestToPlayer extends Thread {
	private Player player;
	private Player player_i;
	private String case_;
	private boolean []check;
	
	public ThreadSendRequestToPlayer(Player pl, Player pl_i, String c, boolean []check_){
		player = pl;
		player_i = pl_i;
		case_ = c;
		check = check_;
		
	}
	
	public void run() {
		switch(case_){
		case "newplayer":
			notifyImIn();
			break;
		case "confirmed":
			confirm();
			break;
		case "notconfirmed":
			notConfirm();
			break;
		case "token":
			sendTokenToNext();
			break;
		}
	}

	private void notConfirm() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("notaccepted\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void confirm() {
		DataOutputStream outputStream = player_i.getSocketOutput();
		try {
			outputStream.writeBytes("accepted\n");
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

	private void notifyImIn() {
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
		}
		
	}

}