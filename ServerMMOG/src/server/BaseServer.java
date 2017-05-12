package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

@Path("/game")
public class BaseServer {
	
	static Map<String, Game> games = new HashMap<String, Game>();
	
	@POST
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/creategame")
	public Response setGame(Game g){
		games.put(g.getGame_name(), g);
		return Response.ok(g.toString()).build();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/allgames")
	public Response getGames(){
		return Response.ok(games.toString()).build();
	}
	
	@GET
	@Path("/getgame/{game}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getGame(@PathParam("game") String game){
		return Response.ok(games.get(game)).build();
	}
	

}
