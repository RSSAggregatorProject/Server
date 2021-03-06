package com.rssaggregatorserver.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.rssaggregatorserver.entities.RSSTasks;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;
import com.rssaggregatorserver.filters.Secured;
import com.rssaggregatorserver.services.DataServices.DataFeedJSONData;
import com.rssaggregatorserver.services.DataServices.DataItemsJSONData;

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
		String token = header.trim();//.substring("Bearer".length()).trim();
		
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
	
	@Secured
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCategories(String s, @HeaderParam("Authorization") String header)
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
						int cat_unread = 0;
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
								feedData.items = new CategoriesItemsJSONData[0];
								
PreparedStatement query_items = database.connection.prepareStatement( "Select * from items WHERE feed_id = ? ORDER BY date DESC");
								
								query_items.setInt(1, feedData.id_feed);
							
								ResultSet results_items = query_items.executeQuery();
								int feed_unread = 0;
								while (results_items.next())
								{							
									PreparedStatement query2 = database.connection.prepareStatement( "Select * from user_items WHERE id_item = ? AND id_user = ? ORDER BY id");

									query2.setInt( 1, results_items.getInt("id"));
									query2.setInt( 2, id_user);
									ResultSet results_user_items = query2.executeQuery();
									
									boolean itemDataRead;
									if (results_user_items.next())
										itemDataRead = results_user_items.getBoolean("read_state");
									else
									{
										PreparedStatement query_user_items = database.connection.prepareStatement( "INSERT INTO user_items (id_user, id_item, read_state, starred)"
																												 + " VALUES (?, ?, ?, ?)");

										query_user_items.setInt( 1, id_user);
										query_user_items.setInt( 2, results_items.getInt("id"));
										query_user_items.setBoolean(3, false);
										query_user_items.setBoolean(4, false);
										
										int status = query_user_items.executeUpdate();
										
										itemDataRead = false;
									}
									if (itemDataRead == false)
										feed_unread++;
								}
								feedData.unread = feed_unread;
								cat_unread += feed_unread;
								lFeedData.add(feedData);	
							}
							

						}
						data.feeds = new CategoriesFeedJSONData[lFeedData.size()];
						data.feeds = lFeedData.toArray(data.feeds);
						data.unread = cat_unread;
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
	
	@Secured
	@GET
	@Path("/{id_cat}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCategorie(String s, @HeaderParam("Authorization") String header, @PathParam("id_cat") int id_cat)
	{
		CategoriesGetResponse response = new CategoriesGetResponse();
		String token = header.trim();
		
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "Select * from Categories WHERE id_user = ? AND id = ? ORDER BY id");

			preparedStatement.setInt( 1, id_user );
			preparedStatement.setInt( 2, id_cat );
			ResultSet results = preparedStatement.executeQuery();
			
			
			CategoriesFeedJSONData						feedData = null;
			CategoriesJSONData							data = null;
			CategoriesItemsJSONData						itemData = null;
			
			List<CategoriesJSONData>					lData = new ArrayList<CategoriesJSONData>();
			List<CategoriesFeedJSONData>				lFeedData = new ArrayList<CategoriesFeedJSONData>();
			List<CategoriesItemsJSONData>				lItemsData = new ArrayList<CategoriesItemsJSONData>();
			
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
						int cat_unread = 0;
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
								
								PreparedStatement query_items = database.connection.prepareStatement( "Select * from items WHERE feed_id = ? ORDER BY date DESC");
								
								query_items.setInt(1, feedData.id_feed);
							
								ResultSet results_items = query_items.executeQuery();
								int feed_unread = 0;
								while (results_items.next())
								{
									itemData = null;
									itemData = new CategoriesItemsJSONData();
									
									itemData.id_item = results_items.getInt("id");
									itemData.id_feed = results_items.getInt("feed_id");
									itemData.title = results_items.getString("title");
									itemData.description = results_items.getString("description");
									itemData.link = results_items.getString("url");
									itemData.pubDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(results_items.getTimestamp("date"));
									
									PreparedStatement query2 = database.connection.prepareStatement( "Select * from user_items WHERE id_item = ? AND id_user = ? ORDER BY id");

									query2.setInt( 1, results_items.getInt("id"));
									query2.setInt( 2, id_user);
									ResultSet results_user_items = query2.executeQuery();
									
									if (results_user_items.next())
									{
										itemData.read = results_user_items.getBoolean("read_state");
										itemData.starred = results_user_items.getBoolean("starred");
									}
									else
									{
										PreparedStatement query_user_items = database.connection.prepareStatement( "INSERT INTO user_items (id_user, id_item, read_state, starred)"
																												 + " VALUES (?, ?, ?, ?)");

										query_user_items.setInt( 1, id_user);
										query_user_items.setInt( 2, results_items.getInt("id"));
										query_user_items.setBoolean(3, false);
										query_user_items.setBoolean(4, false);
										
										int status = query_user_items.executeUpdate();
										
										itemData.read = false;
										itemData.starred = false;
									}
									if (itemData.read == false)
										feed_unread++;
									lItemsData.add(itemData);
								}
								
								feedData.items = new CategoriesItemsJSONData[lItemsData.size()];
								feedData.items = lItemsData.toArray(feedData.items);
								feedData.unread = feed_unread;
								cat_unread += feed_unread;
								lFeedData.add(feedData);
								
								lItemsData = new ArrayList<CategoriesItemsJSONData>();
							}
							

						}
						data.feeds = new CategoriesFeedJSONData[lFeedData.size()];
						data.feeds = lFeedData.toArray(data.feeds);
						data.unread = cat_unread;
						lData.add(data);
						
						lFeedData = new ArrayList<CategoriesFeedJSONData>();
			}
			
			if (i > 0)
			{
				
				response.data = new CategoriesJSONData[lData.size()];
				response.data = lData.toArray(response.data);

			}
			else
				throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_CAT_DOESNT_EXIST));
			
			
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
	
}


class CategoriesItemsJSONData
{
	int 		id_item;
	String		title;
	String		description;
	String		pubDate;
	String		link;
	int			id_feed;
	boolean		read;
	boolean		starred;
	
	public int getId_item(){
		return (id_item);
	}	
	
	public String getTitle(){
		return (title);
	}
	
	public String getDescription(){
		return (description);
	}
	
	public String getPubDate(){
		return (pubDate);
	}
	
	public String getLink(){
		return (link);
	}
	
	public int getId_feed(){
		return (id_feed);
	}
	
	public boolean getRead(){
		return (read);
	}
	
	public boolean getStarred(){
		return (starred);
	}
	
	public void setId_item(int id)
	{
		id_item = id;
	}
	
	public void setTitle(String _title)
	{
		title = _title;
	}
	
	public void setDescription(String str)
	{
		description = str;
	}
	
	public void setPubDate(String str)
	{
		pubDate = str;
	}
	
	public void setLink(String str)
	{
		link = str;
	}
	
	public void setId_feed(int id)
	{
		id_feed = id;
	}
	
	public void setRead(boolean bool)
	{
		read = bool;
	}
	
	public void setStarred(boolean bool)
	{
		starred = bool;
	}
}


class CategoriesFeedJSONData
{
	int 						id_feed;
	String 						name;
	String						favicon_uri;
	int							unread;
	CategoriesItemsJSONData[]	items;
	
	public int getId_feed(){
		return (id_feed);
	}	
	
	public int getUnread(){
		return (unread);
	}	
	
	public void setUnread(int nb)
	{
		unread = nb;
	}
	
	public String getName(){
		return (name);
	}
	
	public CategoriesItemsJSONData[] getItems(){
		return (items);
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

	public void setItems(CategoriesItemsJSONData[] _items)
	{
		items = _items;
	}
}

class CategoriesJSONData
{
	int 								id_cat;
	String 								name;
	//int 			unread;
	int									unread;
	CategoriesFeedJSONData[]			feeds;
	
	public int getUnread(){
		return (unread);
	}	
	
	public void setUnread(int nb)
	{
		unread = nb;
	}
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