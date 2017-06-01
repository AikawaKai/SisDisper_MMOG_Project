
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.URI;
import peer.objects.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.glassfish.jersey.client.ClientConfig;

public class Main {
	public static void main(String[] args) {
		// inizializzazione variabili
		String indirizzo_ip = null;
		String input = null;
		String nickname = null;
		String baseuri = null;
		String serverResponse = "non connesso";
		int porta = 0;
		int scelta = 1;
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		URI basepath = null;
		WebTarget target = null;

		// handler della connessione
		while(!serverResponse.equals("connesso"))
		{
			try{
				System.out.print("Indirizzo ip: ");
				indirizzo_ip = bufferedReader.readLine();
				System.out.print("Porta in ascolto: ");
				input = bufferedReader.readLine();
				porta = Integer.parseInt(input);
				baseuri = "http://"+indirizzo_ip+":"+porta;
				System.out.println("Server: "+baseuri);
				basepath = UriBuilder.fromUri(baseuri).build();
				target = client.target(basepath);
				serverResponse = target.path("ServerMMOG").path("rest").path("game").request().get(String.class);
				target = target.path("ServerMMOG").path("rest").path("game");
				System.out.println(serverResponse);
			}catch(Exception e){
				System.out.println("Dati non corretti / Server non attivo");
			}
		}
		
		// scelta nickname
		try {
			System.out.print("Inserisci il tuo nickname: ");
			nickname = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// menu
		try {
			while(scelta != 5){
				System.out.println("");
				System.out.println("###########################################");
				System.out.println("#                  Menu                   #");
				System.out.println("# 1 - Elenco partite in corso             #");
				System.out.println("# 2 - Visualizza dettaglio di una partita #");
				System.out.println("# 3 - Crea nuova partita                  #");
				System.out.println("# 4 - Aggiungiti ad una partita esistente #");
				System.out.println("# 5 - Exit");
				System.out.println("###########################################");
				System.out.println("Scegli: ");
				input = bufferedReader.readLine();
				scelta = Integer.parseInt(input);
				if(scelta < 1 || scelta > 5){
					System.out.println("Selezione errata");
				}else if(scelta!=5){
					menuHandler(scelta, target);
				}
			}
		} catch (Exception e) {
			System.out.println("Selezione errata");
		}
	}
	private static void menuHandler(int scelta, WebTarget target){
		switch(scelta){
		case 1:
			gamesList(target);
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		default:
		}
	}
	private static void gamesList(WebTarget target) {
		try {
			GamesMap map = new GamesMap();
			JAXBContext ctx = JAXBContext.newInstance(GamesMap.class);
			String serverResponse = target.path("allgames").request().get(String.class);
			map = (GamesMap)ctx.createUnmarshaller().unmarshal(new StringReader(serverResponse));
			map.prettyPrint();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
	}
}