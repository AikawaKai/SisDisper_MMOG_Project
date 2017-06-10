package server;

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
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/")
	public Response checkConnect(){
		return Response.ok("connesso").build();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/creategame")
	public Response setGame(Game g){
		if(!games.put(g.getGame_name(), g))
			return Response.status(HttpServletResponse.SC_CONFLICT).build();
		return Response.created(null).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/allgames")
	public Response getGames(){
		synchronized(games){
			/*
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
			return Response.ok(games).build();
		}
			
	}
	
	@GET
	@Path("/getgame/{game}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGame(@PathParam("game") String game){
		Game g = games.get(game);
		if(g!=null){
			return Response.ok(g).build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
	@DELETE
	@Path("/deletegame/{game}")
	public Response deleteGame(@PathParam("game") String game){
		if(games.remove(game)){
			return Response.ok().build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
	@POST
	@Path("/addplayer/{game}")
	@Consumes(MediaType.APPLICATION_XML)
	public Response addPlayer(@PathParam("game") String game, Player pl){
		Game g = games.get(game);
		if(g!=null){
			if(g.insertPlayer(pl))
				return Response.ok().build();
			return Response.status(HttpServletResponse.SC_CONFLICT).build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
	@DELETE
	@Path("/deleteplayer/{game}/{pl}")
	public Response deletePLayer(@PathParam("game") String game, @PathParam("pl") String pl){
		Game g = games.get(game);
		if(g!=null){
			if(g.removePlayer(pl))
			{
				return Response.ok().build();
			}
			return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
		}
		return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
	}
	
}
