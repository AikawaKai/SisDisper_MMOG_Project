package server;

import java.util.ArrayList;

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

import server.objects.DeletePlayer;
import server.objects.Game;
import server.objects.GamesMap;
import server.objects.Player;

@Path("/game")
public class BaseServer {
	
	static GamesMap games = new GamesMap();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/")
	public Response checkConnect(){
		return Response.ok("connesso").build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_XML)
	@Path("/creategame")
	public Response setGame(Game g){
		boolean res;
		ArrayList<ArrayList<Player>> toaddplayers;
		ArrayList<DeletePlayer> todeleteplayers;
		ThreadConsumerAddPlayer addPlayer;
		ThreadConsumerDeletePlayer delPlayer;
		synchronized(g){ //blocco l'istanza game finché non preparo i suoi thread 
			res = games.put(g.getGame_name(), g); //metodo sincronizzato
			if(!res)
				return Response.status(HttpServletResponse.SC_CONFLICT).build();
			toaddplayers = g.getToAddList();
			todeleteplayers = g.getToDelList();
			addPlayer = new ThreadConsumerAddPlayer(toaddplayers);
			addPlayer.start();
			delPlayer = new ThreadConsumerDeletePlayer(todeleteplayers);
			delPlayer.start();
		}
		return Response.created(null).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/allgames")
	public Response getGames(){
		synchronized(games){ //blocco l'istanza games finché non costruisco il messaggio
			return Response.ok(games).build();
		}
			
	}
	
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
	
	@DELETE
	@Path("/deletegame/{game}")
	public Response deleteGame(@PathParam("game") String game){
		if(games.remove(game)){ //metodo sincronizzato
			return Response.ok().build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
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
	
}
