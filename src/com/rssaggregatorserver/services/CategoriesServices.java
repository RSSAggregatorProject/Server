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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mysql.jdbc.Statement;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.entities.JSONUtils;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.filters.Secured;

@Path("/categories")
public class CategoriesServices {
	
	@Secured
	@Path("/")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response postCategories(String s, @HeaderParam("Authorization") String header)
	{
		ObjectMapper mapper = new ObjectMapper();
		CategoriesPostResponse response = new CategoriesPostResponse();
		CategoriesPostRequest request = null;
		String token = header.substring("Bearer".length()).trim();
		
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		
		try {
			request = mapper.readValue(s, CategoriesPostRequest.class);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			System.out.println(request);
			}
		catch (JsonParseException e) { throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (JsonMappingException e) { throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (IOException e) { throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		
		
		if (request.name == null || request.name == "")
			throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.MISSING_NAME));
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement statement = database.connection.prepareStatement("INSERT INTO Categories (id_user, name) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
			statement.setInt( 1, id_user );
			statement.setString( 2, request.name );

			
			int status = statement.executeUpdate();
			if (status == 0)
				throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_CATEGORIES));
			
			int id_cat = -1;
			
	        try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
	            if (generatedKeys.next()) {
	                id_cat = (int)generatedKeys.getLong(1);
	            }
	            else {
	                throw new SQLException("Creating user failed, no ID obtained.");
	            }
	        }
			
			if (id_cat < 0)
				throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_CATEGORIES));
			
			response.id_cat = id_cat;
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new CustomInternalServerError(Errors.createJSONErrorResponse(e.toString()));
		}
		finally
		{
			database.Disconnect();
		}
		
		response.name = request.name;
		response.status = "success";
		
		return (Response.created(null).entity(JSONUtils.createJSONResponse(response)).type(MediaType.APPLICATION_JSON).build());
	}
	
}


class CategoriesPostResponse
{
	String 	status;
	int 	id_cat;
	String	name;
	
	public int getId_cat()
	{
		return (id_cat);
	}
	
	public void setId_cat(int cat_id)
	{
		this.id_cat = cat_id;
	}
	
	public String getStatus()
	{
		return (status);
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	public String getName()
	{
		return (name);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	  public String toString(){
	      return "CategoriesPostResponse [ status: "+ status +", id_cat: "+ id_cat + ", name: "+ name +" ]";
	   }	
}

class CategoriesPostRequest
{
	String	name;
	
	public String getName()
	{
		return (name);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	  public String toString(){
	      return "CategoriesPostRequest [ name: "+ name +" ]";
	   }	
}