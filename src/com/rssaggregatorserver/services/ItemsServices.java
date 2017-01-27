package com.rssaggregatorserver.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.entities.JSONUtils;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.filters.Secured;

@Path("/items")
public class ItemsServices {

	@PUT
	@Secured	
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response putItems(String s, @HeaderParam("Authorization") String header)
	{
		ObjectMapper mapper = new ObjectMapper();
		ItemPutResponse response = new ItemPutResponse();
		ItemPutRequest  request = null;
		
		String token = header.trim();
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		try {
			 request = mapper.readValue(s, ItemPutRequest.class);
			 mapper.enable(SerializationFeature.INDENT_OUTPUT);
			 System.out.println(request.read);
				
		} catch (JsonParseException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (JsonMappingException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (IOException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			/* On vérifie si la catégorie est bien celle de l'utilisateur */
			PreparedStatement feed_state = database.connection.prepareStatement( "SELECT id_feed, name FROM user_feed WHERE id_user = ?");
			feed_state.setInt( 1, id_user );
			
			
			ResultSet feed_results = feed_state.executeQuery();
			while (feed_results.next())
			{
				PreparedStatement feed_items_update = database.connection.prepareStatement( "SELECT * from items WHERE feed_id = ?");
				feed_items_update.setInt( 1, feed_results.getInt("id_feed"));
				
				ResultSet query = feed_items_update.executeQuery();
				while (query.next())
				{
					PreparedStatement items_update = database.connection.prepareStatement( "UPDATE user_items set read_state = ? WHERE id_user = ? AND id_item = ?");
					items_update.setBoolean( 1, request.read);
					items_update.setInt( 2, id_user );
					items_update.setInt( 3, query.getInt("id"));
					
					int status = items_update.executeUpdate();
				}
			}
			
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
		return (Response.ok().entity(JSONUtils.createJSONResponse(response)).type(MediaType.APPLICATION_JSON).build());
	}
}
	
class ItemPutRequest

{

	boolean 	read;
	
		public boolean getRead() {
			return (read);
		}
		
		
		public void setRead(boolean _read) {
			read = _read;
		}	
	}
	
	class ItemPutResponse
	{
		String 	status;
		
		public void setStatus(String status) {
			this.status = status;
		}
		
		
		public String getStatus() {
			return (this.status);
		}
		
	}
