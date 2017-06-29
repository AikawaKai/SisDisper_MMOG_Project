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
		player_i.sendMessage("checkin CONTENT:"+player.marshallerThis()+"\n");
	}

	// provo ad aggiungermi come giocatore. Viene controllata la posizione
	@SuppressWarnings("unchecked")
	private void notifyNewPlayer() {
		String response = "";
		player_i.sendMessage("newplayer CONTENT:"+player.marshallerThis()+"\n");
		response = player_i.getMessage();
		if(response == null){
			synchronized(check){
				check[0] = true;
				synchronized(result){
					((ArrayList<Player>) result).add(player_i);
				}
			}
			return;
		}
		if(response.equals("ko"))
		{
			synchronized(check){
				check[0] = true;
			}
		}
	}

	// sono riuscito ad entrare, comunico al giocatore di avercela fatta (per poterlo sbloccare)
	private void notifyImIn() {
		player_i.sendMessage("imin\n");
	}

	// metodo per segnalare che sto uscendo
	private void notifyDeletePlayer() {
		player_i.sendMessage("deleteplayer CONTENT:"+player.marshallerThis()+"\n");
		player_i.getMessage();
	}

	// metodo per segnalare la nuova posizione dopo uno spostamento
	private void sendNewPos() {
		String response = "";
		String []status_next;
		player_i.sendMessage("newpos CONTENT:"+player.getPos().marshallerThis()+"\n");
		response = player_i.getMessage();
		status_next = response.split(" ");
		if(status_next[0].equals("colpito"))
		{
			if(player.getMy_next().equals(player_i.getName()))
				player.setMy_next(status_next[1]);
			System.out.println("[INFO] Hai colpito il giocatore ["+player_i.getName()+"]");
			player.addOnePoint();
		}
	}

	// metodo per segnalare della bomba lanciata
	private void notifyBomb() {
		player_i.sendMessage("bomb CONTENT:"+((String) result)+"\n");
		player_i.getMessage();
	}

	// notifica esplosione bomba
	private void notifyExplosion() {
		Bomb b = (Bomb) result;
		String status;
		String []status_next;
		player_i.sendMessage("explosion CONTENT:"+b.getColor()+"\n");
		status = player_i.getMessage();
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
	}

	// metodo per segnalare la propria vittoria
	private void victory() {
		player_i.sendMessage("victory\n");
	}

	// metodo per segnalare che la posizione del giocatore è stata acettata da tutti
	private void accept() {
		String response = "";
		player_i.sendMessage("accepted CONTENT:"+((Player)result).getName()+"\n");
		response = player_i.getMessage();
		Player my_prev = Player.unmarshallThat(new StringReader(response));
		if(((Player)result).equals(player_i))
		{
			player.setMy_next(my_prev.getMy_next());
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
		player_i.sendMessage("notaccepted\n");
	}


	// mando il token al mio next
	private void sendTokenToNext() {
		player_i.sendMessage("token\n");
	}

}
