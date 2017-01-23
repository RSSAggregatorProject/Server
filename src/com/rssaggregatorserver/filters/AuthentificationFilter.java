package com.rssaggregatorserver.filters;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.exceptions.CustomNotAuthorizedException;
import com.rssaggregatorserver.exceptions.CustomNotFoundException;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthentificationFilter implements ContainerRequestFilter{
	@Override
	public void filter(ContainerRequestContext	context) throws IOException
	{
		String authHeader = context.getHeaderString(HttpHeaders.AUTHORIZATION);
		
		if (authHeader == null || !authHeader.startsWith("Bearer ")){
			String tmp = Errors.createJSONErrorResponse(ErrorStrings.AUTH_NOT_PROVIDE);
			throw new CustomNotAuthorizedException(tmp);
		}
		
		String token = authHeader.substring("Bearer".length()).trim();

		validAuthentification(token);
		
		System.out.println("Filtre d'authentification réussi !");
	}
	
	private void validAuthentification(String token) {
		
		DatabaseManager database = DatabaseManager.getInstance();
		
		try { 
				database.Connect();
				
				PreparedStatement preparedStatement = database.connection.prepareStatement( "SELECT id, exp_date FROM Users WHERE token = ?;" );
				preparedStatement.setString( 1, token );
				
				/* Exécution de la requête */
				ResultSet result = preparedStatement.executeQuery();
				
				List<Integer> 	 iDs = new ArrayList<Integer>();
				List<String>	 exp_dates = new ArrayList<String>();
				
				while (result.next())
				{
					iDs.add(result.getInt("id"));
					exp_dates.add(result.getString("exp_date"));
				}
				
				if (iDs.size() > 1)
				{
					String tmpMsg = ErrorStrings.CRITICAL_ERROR;
					
					tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
					throw new CustomInternalServerError(tmpMsg);
				}
				else if (iDs.size() == 0)
				{
					String tmpMsg = ErrorStrings.AUTH_BAD_TOKEN;
					
					tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
					throw new CustomNotAuthorizedException(tmpMsg);
				}
				
				
				DateTime now = new DateTime();
				DateTime exp_date = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
					    .parseDateTime(exp_dates.get(0));
				
				if (now.isAfter(exp_date))
				{
					String tmpMsg = ErrorStrings.AUTH_EXP_DATE;
					
					tmpMsg = Errors.createJSONErrorResponse(tmpMsg);
					throw new CustomNotAuthorizedException(tmpMsg);
				}
				
				
				
			}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally
		{
			database.Disconnect();
		}
	}
	
}
