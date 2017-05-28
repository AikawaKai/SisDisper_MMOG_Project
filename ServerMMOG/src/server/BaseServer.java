package server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import server.objects.Game;
import server.objects.GamesMap;

@Path("/game")
public class BaseServer {
	
	static GamesMap games = new GamesMap();
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/creategame")
	public Response setGame(Game g){
		if(games.containsKey(g.getGame_name()))
			return Response.noContent().build();
		games.put(g.getGame_name(), g);
		return Response.created(null).build();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("/allgames")
	public Response getGames(){
		return Response.ok(games).build();
	}
	
	@GET
	@Path("/getgame/{game}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGame(@PathParam("game") String game){
		if(games.containsKey(game)){
			return Response.ok(games.get(game)).build();
		}
		return Response.noContent().build();
	}
	
}
