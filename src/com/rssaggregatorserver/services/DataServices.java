package com.rssaggregatorserver.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.entities.JSONUtils;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.filters.Secured;

@Path("/data")
public class DataServices {

	@Secured
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getData(String s, @HeaderParam("Authorization") String header)
	{
		ObjectMapper mapper = new ObjectMapper();
		CategoriesGetResponse response = new CategoriesGetResponse();
		String token = header.trim();
		
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "Select * from Categories WHERE id_user = ? ORDER BY id");

			preparedStatement.setInt( 1, id_user );
			ResultSet results = preparedStatement.executeQuery();
			
			
			CategoriesFeedJSONData		feedData = null;
			CategoriesJSONData			data = null;
			
			List<CategoriesJSONData>	lData = new ArrayList<CategoriesJSONData>();
			List<CategoriesFeedJSONData>	lFeedData = new ArrayList<CategoriesFeedJSONData>();
			
			int i = -1;
			while (results.next())
			{				
						i = results.getInt("id");

						data = new CategoriesJSONData();
						data.id_cat = i;
						data.name = results.getString("name");
						
						PreparedStatement query_feed_cat = database.connection.prepareStatement( "Select id_feed from user_feed WHERE id_cat = ? ORDER BY id");
						
						query_feed_cat.setInt(1, i);
					
						ResultSet results_feed_cat = query_feed_cat.executeQuery();
						while (results_feed_cat.next())
						{
							feedData = null;
							PreparedStatement query = database.connection.prepareStatement( "Select * from feeds WHERE id = ? ORDER BY id");

							query.setInt( 1, results_feed_cat.getInt("id_feed"));
							ResultSet results_feed = query.executeQuery();
						
						
							while (results_feed.next())
							{
								feedData = new CategoriesFeedJSONData();
						
								feedData.name = results_feed.getString("name");
								feedData.favicon_uri = "favicon_not_implemented";
								feedData.id_feed = results_feed.getInt("id");
						
								lFeedData.add(feedData);	
							}
							

						}
						data.feeds = new CategoriesFeedJSONData[lFeedData.size()];
						data.feeds = lFeedData.toArray(data.feeds);
						lData.add(data);
						
						lFeedData = new ArrayList<CategoriesFeedJSONData>();
			}
			
			if (i > 0)
			{
				
				response.data = new CategoriesJSONData[lData.size()];
				response.data = lData.toArray(response.data);

			}
			
			
		}
		catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new CustomInternalServerError(Errors.createJSONErrorResponse(e.getMessage()));
		}
		finally
		{
			database.Disconnect();
		}
		
		response.status = "success";
		
		return (Response.ok().entity(JSONUtils.createJSONResponse(response)).type(MediaType.APPLICATION_JSON).build());
	}
	
	class CategoriesFeedJSONData
	{
		int 		id_feed;
		String 		name;
		String		favicon_uri;
		
		public int getId_feed(){
			return (id_feed);
		}	
		
		public String getName(){
			return (name);
		}
		
		public String getFavicon_uri(){
			return (favicon_uri);
		}
		
		public void setId_feed(int id)
		{
			id_feed = id;
		}
		
		public void setName(String _name)
		{
			name = _name;
		}
		
		public void setFavicon_uri(String _favicon)
		{
			favicon_uri = _favicon;
		}
	}

	class CategoriesJSONData
	{
		int 								id_cat;
		String 								name;
		//int 			unread;
		CategoriesFeedJSONData[]			feeds;
		
		public int getId_cat(){
			return (id_cat);
		}	
		
		public String getName(){
			return (name);
		}
		
		public CategoriesFeedJSONData[] getFeeds(){
			return (feeds);
		}
		
		public void setId_cat(int id)
		{
			id_cat = id;
		}
		
		public void setName(String _name)
		{
			name = _name;
		}
		
		public void setFeeds(CategoriesFeedJSONData[] _feeds)
		{
			feeds = _feeds;
		}
	}

	class CategoriesGetResponse
	{
		String								status;
		CategoriesJSONData[]				data;
		
		public String getStatus(){
			return (status);
		}
		
		public CategoriesJSONData[] getData(){
			return (data);
		}
		
		public void setStatus(String _status)
		{
			status = _status;
		}
		
		public void setData(CategoriesJSONData[] _data)
		{
			data = _data;
		}
		
		public String toString(){
		      return "CategoriesGetResponse [ status: "+ status +
		    		  				", data: "+ data + "]";
		   }	
	}
}
