package com.rssaggregatorserver.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
		DataGetResponse response = new DataGetResponse();
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
			
			
			DataFeedJSONData					feedData = null;
			DataCategoriesJSONData				data = null;
			DataItemsJSONData					itemData = null;
			
			List<DataCategoriesJSONData>		lData = new ArrayList<DataCategoriesJSONData>();
			List<DataFeedJSONData>				lFeedData = new ArrayList<DataFeedJSONData>();
			List<DataItemsJSONData>				lItemsData = new ArrayList<DataItemsJSONData>();
			
			int i = -1;
			while (results.next())
			{				
						i = results.getInt("id");

						data = new DataCategoriesJSONData();
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
								feedData = new DataFeedJSONData();
						
								feedData.name = results_feed.getString("name");
								feedData.favicon_uri = "favicon_not_implemented";
								feedData.id_feed = results_feed.getInt("id");
								
								PreparedStatement query_items = database.connection.prepareStatement( "Select * from items WHERE feed_id = ? ORDER BY date DESC");
								
								query_items.setInt(1, feedData.id_feed);
							
								ResultSet results_items = query_items.executeQuery();
								while (results_items.next())
								{
									itemData = null;
									itemData = new DataItemsJSONData();
									
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
									
									lItemsData.add(itemData);
								}
								feedData.items = new DataItemsJSONData[lItemsData.size()];
								feedData.items = lItemsData.toArray(feedData.items);
								lFeedData.add(feedData);	
								
								lItemsData = new ArrayList<DataItemsJSONData>();
							}
							

						}
						data.feeds = new DataFeedJSONData[lFeedData.size()];
						data.feeds = lFeedData.toArray(data.feeds);
						lData.add(data);
						
						lFeedData = new ArrayList<DataFeedJSONData>();
			}
			
			if (i > 0)
			{
				
				response.data = new DataCategoriesJSONData[lData.size()];
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
	
	class DataFeedJSONData
	{
		int 								id_feed;
		String 								name;
		String								favicon_uri;
		DataItemsJSONData[]					items;
		
		public int getId_feed(){
			return (id_feed);
		}	
		
		public String getName(){
			return (name);
		}
		
		public String getFavicon_uri(){
			return (favicon_uri);
		}
		
		public DataItemsJSONData[] getItems(){
			return (items);
		}
		
		public void setId_feed(int id)
		{
			id_feed = id;
		}
		
		public void setItems(DataItemsJSONData[] _items)
		{
			items = _items;
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

	class DataCategoriesJSONData
	{
		int 								id_cat;
		String 								name;
		//int 			unread;
		DataFeedJSONData[]					feeds;
		
		public int getId_cat(){
			return (id_cat);
		}	
		
		public String getName(){
			return (name);
		}
		
		public DataFeedJSONData[] getFeeds(){
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
		
		public void setFeeds(DataFeedJSONData[] _feeds)
		{
			feeds = _feeds;
		}
	}

	class DataItemsJSONData
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
	
	class DataGetResponse
	{
		String									status;
		DataCategoriesJSONData[]				data;
		
		public String getStatus(){
			return (status);
		}
		
		public DataCategoriesJSONData[] getData(){
			return (data);
		}
		
		public void setStatus(String _status)
		{
			status = _status;
		}
		
		public void setData(DataCategoriesJSONData[] _data)
		{
			data = _data;
		}
		
		public String toString(){
		      return "CategoriesGetResponse [ status: "+ status +
		    		  				", data: "+ data + "]";
		   }	
	}
}
