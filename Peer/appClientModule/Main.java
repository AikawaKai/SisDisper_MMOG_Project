
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

public class Main {
	public static void main(String[] args) {
		// inizializzazione variabili
		String indirizzo_ip = null;
		String input = null;
		String nickname = null;
		int porta = 0;
		String serverResponse = "non connesso";
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		UriBuilder.fromUri("http://localhost:8080/com.vogella.jersey.first").build();
		String baseuri = null;
		URI basepath = null;

		// handler della connessione
		while(!serverResponse.equals("connesso"))
		{
			try{
				System.out.println("Indirizzo ip: ");
				indirizzo_ip = bufferedReader.readLine();
				System.out.println("Porta in ascolto: ");
				input = bufferedReader.readLine();
				porta = Integer.parseInt(input);
				System.out.println("Indirizzo ip: "+indirizzo_ip + "| porta: "+porta);
				baseuri = "http://"+indirizzo_ip+":"+porta;
				basepath = UriBuilder.fromUri(baseuri).build();
				WebTarget target = client.target(basepath);
				serverResponse = target.path("ServerMMOG").path("rest").path("game").request().get(String.class);
				System.out.println(serverResponse);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println("Dati non corretti");
			}
		}

		try {
			System.out.print("Inserisci il tuo nickname: ");
			nickname = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}