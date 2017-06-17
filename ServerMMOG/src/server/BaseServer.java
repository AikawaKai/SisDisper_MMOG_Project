package server;

import java.util.Random;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import server.objects.Game;
import server.objects.GamesMap;
import server.objects.Player;

@Path("/game")
public class BaseServer {
	
	static GamesMap games = new GamesMap();
	
	// metodo REST per la verifica dei dati per le REST request
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/")
	public Response checkConnect(){
		return Response.ok("connesso").build();
	}
	
	// metodo REST per la creazione di una martita
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/creategame")
	public Response setGame(Game g){
		boolean res;
		res = games.put(g.getGame_name(), g); //metodo sincronizzato
		if(!res)
			return Response.status(HttpServletResponse.SC_CONFLICT).build();
		return Response.created(null).build();
	}

	// metodo REST per la restituzione di tutte le partite in corso
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/allgames")
	public Response getGames(){
		synchronized(games){ //blocco l'istanza games finché non costruisco il messaggio
			return Response.ok(games).build();
		}
			
	}
	
	// metodo REST per la restituzione dell'istanza di una partita
	@GET
	@Path("/getgame/{game}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGame(@PathParam("game") String game){
		Game g = games.get(game);
		if(g!=null){
			synchronized(g){
				return Response.ok(g).build();
			}
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
	// metodo REST per la cancellazione di una partita
	@DELETE
	@Path("/deletegame/{game}")
	public Response deleteGame(@PathParam("game") String game){
		if(games.remove(game)){ //metodo sincronizzato
			return Response.ok().build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
	// metodo REST per l'aggiunta di un giocatore
	@POST
	@Path("/addplayer/{game}")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	public Response addPlayer(@PathParam("game") String game, Player pl){
			int res;
			Game g;
			res = games.addPlayer(game, pl); //metodo sincronizzato
			g = games.get(game); // metodo sincronizzato
			if(g!=null)
			{
				synchronized(g) // sincronizzo per evitare modifiche mentre sto inviando il dato
				{
					if(res==1){
						return Response.ok(g).build();
					}
				}
			}
			if(res==0)
				return Response.status(HttpServletResponse.SC_CONFLICT).build();
			return Response.status(HttpServletResponse.SC_NOT_FOUND).build();		
	}
	
	// metodo REST per la cancellazione di un giocatore
	@DELETE
	@Path("/deleteplayer/{game}/{pl}")
	public Response deletePLayer(@PathParam("game") String game, @PathParam("pl") String pl){
		int res = games.removePlayer(game, pl);
		if(res==1)
			return Response.ok().build();
		if(res==0)
			return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
	public static int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
}
