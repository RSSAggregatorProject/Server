package com.rssaggregatorserver.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.DateTime;

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
import com.rssaggregatorserver.exceptions.CustomNotFoundException;

@Path("/users")
public class UsersServices {
	
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(String s)
	{
		ObjectMapper mapper = new ObjectMapper();
		UserPostRequest request = null;
		UserPostResponse response = new UserPostResponse();
		
		try {
			request = mapper.readValue(s, UserPostRequest.class);
			System.out.println(request);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			}
		catch (JsonParseException e) { throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (JsonMappingException e) { throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (IOException e) { throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		
		if (request.password == null || request.email == null)
		{
			String tmpMsg = request.email == null ? ErrorStrings.MISSING_EMAIL : ErrorStrings.MISSING_PASS;
			
			tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
			throw new CustomBadRequestException(tmpMsg);
		}
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement statement = database.connection.prepareStatement("SELECT email FROM Users WHERE email = ?");
			statement.setString( 1, request.email );

			/* Exécution de la requête */
			ResultSet result = statement.executeQuery();
			
			List<String> emails = new ArrayList<String>();
			
			while (result.next())
				emails.add(result.getString("email"));
			
			if (emails.size() > 0)
				throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.EMAIL_ALREADY_USED));
			
			statement = database.connection.prepareStatement("INSERT INTO Users (email, password, token, exp_date) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			statement.setString( 1, request.email );
			statement.setString( 2, request.password );
			statement.setString( 3, "chuck_norris");
			statement.setString( 4, "1970-01-01 00:00:00");
			
			int status = statement.executeUpdate();
			if (status == 0)
				throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTRATION));
			
			int user_id = -1;
			
			 try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
		            if (generatedKeys.next()) {
		                user_id = (int)generatedKeys.getLong(1);
		            }
		            else {
		                throw new SQLException("Creating user failed, no ID obtained.");
		            }
		        }
				
				if (user_id < 0)
					throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_CATEGORIES));
			
			response.id_user = user_id;
			
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			database.Disconnect();
		}
		
		response.status = "success";
		return (Response.created(null).entity(JSONUtils.createJSONResponse(response)).type(MediaType.APPLICATION_JSON).build());
	}


	
}


class UserPostResponse
{
	String 	status;
	int 	id_user;
	
	public int getId_user()
	{
		return (id_user);
	}
	
	public void setId_user(int user_id)
	{
		this.id_user = user_id;
	}
	
	public String getStatus()
	{
		return (status);
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	
	  public String toString(){
	      return "UserPostRequest [ status: "+ status +", id_user: "+ id_user + " ]";
	   }	
}

class UserPostRequest
{
	String email;
	String password;
	
	public String getPassword()
	{
		return (password);
	}
	
	public void setPassword(String pass)
	{
		this.password = pass;
	}
	
	public String getEmail()
	{
		return (email);
	}
	
	public void setEmail(String email)
	{
		this.email = email;
	}
	
	  public String toString(){
	      return "UserPostRequest [ email: "+ email +", pass: "+ password + " ]";
	   }	
}
