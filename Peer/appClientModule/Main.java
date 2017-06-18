
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Random;

import peer.objects.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.client.ClientConfig;

public class Main {
	public static void main(String[] args) {
		// inizializzazione variabili
		String indirizzo_ip = "";
		String nickname = "";
		String game = "";
		String baseuri = "";
		Response serverResponse;
		Invocation.Builder invocationBuilder;
		int status = 0;
		int porta = 0;
		int scelta = 1;
		int porta_peer = 0;
		ServerSocket welcomeSocket = null;
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		URI basepath = null;
		WebTarget target = null;
		Player player = new Player();

		// handler della connessione
		while(status != 200)
		{
			try{
				System.out.print("Indirizzo ip: ");
				indirizzo_ip = bufferedReader.readLine();
				System.out.print("Porta in ascolto: ");
				porta = integerReaderHandler(bufferedReader);
				baseuri = "http://"+indirizzo_ip+":"+porta;
				System.out.println("Server: "+baseuri);
				basepath = UriBuilder.fromUri(baseuri).build();
				target = client.target(basepath);
				target = target.path("ServerMMOG").path("rest").path("game");
				invocationBuilder =  target.request();
				serverResponse = invocationBuilder.get();
				status = serverResponse.getStatus();
				if(status == 200)
					System.out.println(serverResponse.readEntity(String.class));
				else
					System.out.println("[INFO] Dati non corretti / Server non attivo.");
				
			}catch(Exception e){
				System.out.println("[INFO] Dati non corretti / Server non attivo.");
			}
		}
		
		// scelta nickname
		boolean val = true;
		while(val)
		{
			try {
				System.out.print("Inserisci il tuo nickname: ");
				nickname = bufferedReader.readLine();
				val= false;
			} catch (IOException e1) {
				System.out.println("[INFO] Errore inserimento. ");
			}
		}
		val = true;
		while(val){
			// 49152-65535
			try {
				porta_peer = randInt(49152, 65535);
				welcomeSocket = new ServerSocket(porta_peer);
				val = false;
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		player.setIp("localhost");
		player.setPort(porta_peer);
		player.setName(nickname);
		player.setMy_next(nickname);
		
		// menu
		while(scelta != 5){
			System.out.println("");
			System.out.println("###########################################");
			System.out.println("#                  [MENU]                 #");
			System.out.println("# 1 - Elenco partite in corso             #");
			System.out.println("# 2 - Visualizza dettaglio di una partita #");
			System.out.println("# 3 - Crea nuova partita                  #");
			System.out.println("# 4 - Aggiungiti ad una partita esistente #");
			System.out.println("# 5 - Exit                                #");
			System.out.println("###########################################");
			System.out.print("Scegli: ");
			scelta = integerReaderHandler(bufferedReader);
			if(scelta < 1 || scelta > 5){
				System.out.println("[INFO] Selezione errata.");
			}else if(scelta!=5){
				menuHandler(scelta, target, player, game, welcomeSocket);
			}
		}
		
	}
	private static void menuHandler(int scelta, WebTarget target, Player player, String game, ServerSocket welcomeSocket){
		switch(scelta){
		case 1:
			gamesList(target);
			break;
		case 2:
			gameDetails(target);
			break;
		case 3:
			createGame(target, player, welcomeSocket);
			break;
		case 4:
			addPlayerToGame(target, player, game, 0, welcomeSocket);
		default:
		}
	}
	
	private static void gamesList(WebTarget target) {
		GamesMap map = new GamesMap();
		Invocation.Builder invocationBuilder =  target.path("allgames").request();
		Response serverResponse = invocationBuilder.get();
		if(serverResponse.getStatus()==200)
		{
			map = serverResponse.readEntity(GamesMap.class);
			map.gamesList();
		}else
		{
			System.out.println("[INFO] Problemi di connessione.");
		}
		
	}
	
	private static void gameDetails(WebTarget target) {
		try{
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Nome partita: ");
			String name = bufferedReader.readLine();
			Game game = new Game();
			Invocation.Builder invocationBuilder =  target.path("getgame").path(name).request(MediaType.APPLICATION_XML);
			Response serverResponse = invocationBuilder.get();
			if(serverResponse.getStatus()==200)
			{
				game = serverResponse.readEntity(Game.class);
				System.out.println("----Dettaglio Partita----");
				System.out.println(game);
			}else{
				System.out.println("[INFO] Partita inesistente.");
			}
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private static void createGame(WebTarget target, Player player, ServerSocket welcomeSocket) {
		Game game = null;
		int status = 0;
		Invocation.Builder invocationBuilder;
		Response response;
		String GameName = "";
		while(status!=201)
		{
			try{
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Inserisci il nome della partita: ");
				GameName = bufferedReader.readLine();
				System.out.print("Inserisci la larghezza del quadrato di gioco: ");
				int N = integerReaderHandler(bufferedReader);
				System.out.print("Inserisci il numero di punti della partita: ");
				int points = integerReaderHandler(bufferedReader);
				game = new Game();
				game.setGame_name(GameName);
				game.setMax_point(points);
				game.setSize_x(N);
				game.addPlayer(player);
				invocationBuilder =  target.path("creategame").request(MediaType.APPLICATION_XML);
				response = invocationBuilder.post(Entity.entity(game, MediaType.APPLICATION_XML));
				status = response.getStatus();
				if(status==201)
				{
					System.out.println("[INFO] Creazione avvenuta con successo.");
				}
				else if(status==409){
					System.out.println("[INFO] Nome partita già presente.");
					return;
				}else
					System.out.println("[INFO] Creazione partita fallita.");
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		play(target, player.getName(), game, welcomeSocket, true);
	}
	
	private static void addPlayerToGame(WebTarget target, Player player, String game_name, int type, ServerSocket welcomeSocket)
	{
		Game game;
		if(type==0){
			try {
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Inserisci il nome della partita a cui vuoi unirti: ");
				game_name = bufferedReader.readLine();
			} catch (IOException e) {
				System.out.println("[INFO] Aggiunta giocatore alla partita fallita. ");
				return;
			}
		}
		int status = 0;
		Invocation.Builder invocationBuilder;
		Response response;
		invocationBuilder = target.path("addplayer").path(game_name).request(MediaType.APPLICATION_XML);
		response = invocationBuilder.post(Entity.entity(player, MediaType.APPLICATION_XML));
		status = response.getStatus();
		if(status==200){
			System.out.println("Giocatore "+player.getName()+" aggiunto alla partita "+game_name);
			game = response.readEntity(Game.class);
			player = game.getPlayer(player.getName());
			play(target, player.getName(), game, welcomeSocket, false);
		}else if(status==406){
			System.out.println("[INFO] Partita inesistente. Impossibile aggiungere il giocatore. ");
		}else if(status==409){
			System.out.println("[INFO] Giocatore con medesimo nickname già presente in partita. ");
		}else{
			System.out.println("[INFO] Aggiunta giocatore alla partita fallita. ");
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
				System.out.println("[INFO] Dato errato, riprova.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return number;
		
	}
	
	private static void play(WebTarget target, String my_name, Game game, ServerSocket welcomeSocket, boolean first) {
		ThreadPlayingGame playing = new ThreadPlayingGame(target, my_name, game, welcomeSocket);
		try {
			playing.start();
			if(first){
				ThreadSendRequestToPlayer pl_hl = new ThreadSendRequestToPlayer(game.getPlayer(my_name), game.getPlayer(my_name), "token", new boolean[1]);
				pl_hl.start();
			}
			playing.join();
		} catch (InterruptedException e) {
			System.out.println("[INFO] Processo game interrotto.");
		}
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
}