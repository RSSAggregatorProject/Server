package com.rssaggregatorserver.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.sql.Statement;

import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.exceptions.CustomNotFoundException;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.enums.ErrorStrings;

@Path("/auth")
public class AuthServices {
	
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response		getAuthentification(String s) 
	{
		ObjectMapper mapper = new ObjectMapper();
		AuthRequest request = null;
		AuthResponse response = new AuthResponse();
		
		try {
			request = mapper.readValue(s, AuthRequest.class);
			System.out.println(request);
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			}
		catch (JsonParseException e) {e.printStackTrace();}
		catch (JsonMappingException e) {e.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
		
		if (request.password == null || request.email == null)
		{
			String tmpMsg = request.email == null ? ErrorStrings.MISSING_EMAIL : ErrorStrings.MISSING_PASS;
			
			tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
			throw new CustomBadRequestException(tmpMsg);
		}
		
		DatabaseManager database = DatabaseManager.getInstance();
		
		try { 
			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "SELECT * FROM Users WHERE email = ? and password = ?;" );

			preparedStatement.setString( 1, request.email );
			preparedStatement.setString( 2, request.password );

			/* Exécution de la requête */
			ResultSet result = preparedStatement.executeQuery();
			
			List<String> tokens = new ArrayList<String>();
			List<Integer> 	 iDs = new ArrayList<Integer>();
			List<String> exp_dates = new ArrayList<String>();
			
			while (result.next())
			{
				tokens.add(result.getString("token"));
				iDs.add(result.getInt("id"));
				exp_dates.add(result.getString("exp_date"));
			}
			
			if (tokens.size() > 1)
			{
				String tmpMsg = ErrorStrings.CRITICAL_ERROR;
				
				tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
				throw new CustomInternalServerError(tmpMsg);
			}
			else if (tokens.size() == 0)
			{
				String tmpMsg = ErrorStrings.UNKNOWN_USER;
				
				tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
				throw new CustomNotFoundException(tmpMsg);
			}
			
			response.status = "success";
			response.id_user = iDs.get(0).toString();
			response.token = tokens.get(0);
			response.exp_date = exp_dates.get(0);
			
		} 
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			database.Disconnect();
		}
		return (Response.ok().entity(createJSONAuthResponse(response)).type(MediaType.APPLICATION_JSON).build());
	}
	
	String createJSONAuthResponse(AuthResponse response)
	{
		String tmp = "";
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			tmp = mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			tmp = "{ \"status\":\"error\", \"error\":\"Error parsing auth reponse !\"}";
		}
		
		return (tmp);
	}
}

class AuthResponse
{
	String status;
	String id_user;
	String token;
	String exp_date;
	
	public String getStatus()
	{

		return (status);
	}
	
	public String getId_user()
	{
		return (id_user);
	}
	
	public String getToken()
	{
		return (token);
	}
	
	public String getExp_date()
	{
		return (exp_date);
	}
	
	public void setStatus(String status)
	{
		this.status = status;
	}
	public void setId_user(String id_user)
	{
		this.id_user = id_user;
	}
	
	public void setToken(String token)
	{
		this.token = token;
	}
	
	public void setExp_date(String exp_date)
	{
		this.exp_date = exp_date;
	}
	
	  public String toString(){
	      return "AuthRequest [ status: "+ status +
	    		  				", id_user: "+ id_user + 
	    		  				", token: "+ token + 
	    		  				", exp_date: "+ exp_date + " ]";
	   }	
}

class AuthRequest
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
	      return "AuthRequest [ email: "+ email +", pass: "+ password + " ]";
	   }	
}
