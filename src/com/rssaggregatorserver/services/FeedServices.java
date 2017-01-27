package com.rssaggregatorserver.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mysql.jdbc.Statement;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.entities.JSONUtils;
import com.rssaggregatorserver.entities.RSSTasks;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.exceptions.CustomNotAuthorizedException;
import com.rssaggregatorserver.filters.Secured;

@Path("/feeds")
public class FeedServices {

	@POST
	@Secured	
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postFeeds(String s, @HeaderParam("Authorization") String header)
	{
		ObjectMapper mapper = new ObjectMapper();
		FeedPostRequest request = null;
		FeedPostResponse response = new FeedPostResponse();
		
		String token = header.trim();
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		String feed_name = "";
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		
		try {
			 request = mapper.readValue(s, FeedPostRequest.class);
			 mapper.enable(SerializationFeature.INDENT_OUTPUT);
			 System.out.println(request);
				
		} catch (JsonParseException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (JsonMappingException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (IOException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		
		if (request.uri == null)
			Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_URI_MISSING);
		
		/*if (request.name == null)
			Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_URI_MISSING);*/
			
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			/* On vérifie si la catégorie est bien celle de l'utilisateur */
			PreparedStatement cat_state = database.connection.prepareStatement( "SELECT id FROM Categories WHERE id_user = ? AND id = ?");
			cat_state.setInt( 1, id_user );
			cat_state.setInt( 2, request.id_cat );
			
			ResultSet cat_results = cat_state.executeQuery();
			if (!cat_results.next())
				throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_CAT_DOESNT_EXIST));
			
			PreparedStatement preparedStatement = database.connection.prepareStatement( "SELECT * FROM feeds WHERE feed_link = ?");

			preparedStatement.setString( 1, request.uri );
			
			int feed_id = -1;

			/* Exécution de la requête */
			ResultSet result = preparedStatement.executeQuery();
			
			List<String> uris = new ArrayList<String>();
			List<String> names = new ArrayList<String>();
			List<Integer> iDs = new ArrayList<Integer>();
			
			while (result.next())
			{
				uris.add(result.getString("feed_link"));
				iDs.add(result.getInt("id"));
				names.add(result.getString("name"));
			}

			/* Si le feed n'est pas dans la base de données ont le créer.
			 * 
			 *  Sinon on ajoute le résultat sorti.
			 *  
			 *  */
			if (iDs.size() == 0)
			{
				
				/* Avant d'ajouter un feed il faudra faire la fonction de parsing du lien */
				if (!RSSTasks.readFeedUrl(request.uri))
					throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_URI_INCORRECT));
				
				feed_name = RSSTasks.getFeedName(request.uri);
				
				PreparedStatement statement = database.connection.prepareStatement("INSERT INTO feeds (feed_link, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
				statement.setString( 1, request.uri );
				statement.setString( 2, feed_name );

				
				int status = statement.executeUpdate();
				if (status == 0)
					throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_FEEDS));
				
		        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
		            if (generatedKeys.next()) {
		                feed_id = (int)generatedKeys.getLong(1);
		            }
		            else {
		                throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_FEEDS));
		            }
		        }
		        
				
				if (feed_id < 0)
					throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_FEEDS));
				
				// On crée les items relatifs à cette ID (Lecture du flux)
				RSSTasks.readFeeds(request.uri, feed_id);
				
			}
			else
			{
				feed_id = iDs.get(0);
				feed_name = names.get(0);
			}
			
			// On procède au link de la catégorie et du feed.
			PreparedStatement statement = database.connection.prepareStatement( "SELECT * FROM user_feed WHERE id_feed = ? AND id_user = ?");
			statement.setInt( 1, feed_id);
			statement.setInt( 2, id_user);
			ResultSet results = statement.executeQuery();
			
			iDs = new ArrayList<Integer>();
			
			while (results.next())
				iDs.add(results.getInt("id"));
			
			if (iDs.size() == 0)
			{
				//request.name = RSSTasks.getFeedName(request.uri);
				
				statement = database.connection.prepareStatement("INSERT INTO user_feed (id_cat, id_feed, name, id_user) VALUES (?, ?, ?, ?)");
				statement.setInt( 1, request.id_cat );
				statement.setInt( 2, feed_id );
				statement.setString( 3, feed_name );
				statement.setInt(4, id_user);
			
				int status = statement.executeUpdate();
				if (status == 0)
					throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_FEEDS));
			}
			
			response.id_feed = feed_id;
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new CustomInternalServerError(Errors.createJSONErrorResponse(e.toString()));
		}
		finally
		{
			database.Disconnect();
		}
		
		response.status = "success";
		
		return (Response.created(null).entity(JSONUtils.createJSONResponse(response)).type(MediaType.APPLICATION_JSON).build());
	}
	
}

class FeedPostResponse
{
	String 	status;
	int		id_feed; 
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setId_feed(int feed_id) {
		this.id_feed = feed_id;
	}
	
	public String getStatus() {
		return (this.status);
	}
	
	public int getId_feed() {
		return (this.id_feed);
	}
}


class FeedPostRequest
{
	int 		id_cat;
	String	 	name;
	String		uri;
	
	public int getId_cat() {
		return (id_cat);
	}
	
	public String getName() {
		return (name);
	}
	
	public String getUri() {
		return (uri);
	}
	
	public void setId_cat(int cat_id) {
		id_cat = cat_id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
}