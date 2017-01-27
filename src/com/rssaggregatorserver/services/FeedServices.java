package com.rssaggregatorserver.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import com.rssaggregatorserver.exceptions.CustomNotFoundException;
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
			else
				throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_ALREADY_EXIST));
			
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
	
	@GET
	@Secured	
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFeeds(String s, @HeaderParam("Authorization") String header)
	{
		ObjectMapper mapper = new ObjectMapper();
		FeedsGetResponse response = new FeedsGetResponse();
		String token = header.trim();
		
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "Select * from user_feed WHERE id_user = ? ORDER BY id_feed");

			preparedStatement.setInt( 1, id_user );
			ResultSet results = preparedStatement.executeQuery();
			
			
			FeedItemsJSONData		itemData = null;
			FeedJSONData			data = null;
			
			List<FeedJSONData>	lData = new ArrayList<FeedJSONData>();
			List<FeedItemsJSONData>	lItemsData = new ArrayList<FeedItemsJSONData>();
			
			int i = -1;
			while (results.next())
			{				
						i = results.getInt("id_feed");

						data = new FeedJSONData();
						data.id_feed = i;
						data.name = results.getString("name");
						
						PreparedStatement query_items = database.connection.prepareStatement( "Select * from items WHERE feed_id = ? ORDER BY id");
						
						query_items.setInt(1, i);
					
						ResultSet results_items = query_items.executeQuery();
						while (results_items.next())
						{
							itemData = null;
							itemData = new FeedItemsJSONData();
							
							itemData.id_item = results_items.getInt("id");
							itemData.id_feed = results_items.getInt("feed_id");
							itemData.title = results_items.getString("title");
							itemData.description = results_items.getString("description");
							itemData.link = results_items.getString("url");
							itemData.pubDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(results_items.getTimestamp("date"));
							
							PreparedStatement query = database.connection.prepareStatement( "Select * from user_items WHERE id_item = ? AND id_user = ? ORDER BY id");

							query.setInt( 1, results_items.getInt("id"));
							query.setInt( 2, id_user);
							ResultSet results_user_items = query.executeQuery();
							
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
						data.items = new FeedItemsJSONData[lItemsData.size()];
						data.items = lItemsData.toArray(data.items);
						lData.add(data);
						
						lItemsData = new ArrayList<FeedItemsJSONData>();
			}
			
			if (i > 0)
			{
				
				response.data = new FeedJSONData[lData.size()];
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
	
	@GET
	@Secured	
	@Path("/starred")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFeedsStarred(String s, @HeaderParam("Authorization") String header)
	{
		ObjectMapper mapper = new ObjectMapper();
		FeedsGetResponse response = new FeedsGetResponse();
		String token = header.trim();
		
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "Select * from user_feed WHERE id_user = ? ORDER BY id_feed");

			preparedStatement.setInt( 1, id_user );
			ResultSet results = preparedStatement.executeQuery();
			
			
			FeedItemsJSONData		itemData = null;
			FeedJSONData			data = null;
			
			List<FeedJSONData>	lData = new ArrayList<FeedJSONData>();
			List<FeedItemsJSONData>	lItemsData = new ArrayList<FeedItemsJSONData>();
			
			int i = -1;
			while (results.next())
			{				
						i = results.getInt("id_feed");

						data = new FeedJSONData();
						data.id_feed = i;
						data.name = results.getString("name");
						
						PreparedStatement query_items = database.connection.prepareStatement( "Select * from items WHERE feed_id = ? ORDER BY id");
						
						query_items.setInt(1, i);
					
						ResultSet results_items = query_items.executeQuery();
						while (results_items.next())
						{
							itemData = null;
							itemData = new FeedItemsJSONData();
							
							itemData.id_item = results_items.getInt("id");
							itemData.id_feed = results_items.getInt("feed_id");
							itemData.title = results_items.getString("title");
							itemData.description = results_items.getString("description");
							itemData.link = results_items.getString("url");
							itemData.pubDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(results_items.getTimestamp("date"));
							
							PreparedStatement query = database.connection.prepareStatement( "Select * from user_items WHERE id_item = ? AND id_user = ? AND starred=true ORDER BY id");

							query.setInt( 1, results_items.getInt("id"));
							query.setInt( 2, id_user);
							ResultSet results_user_items = query.executeQuery();
							
							if (results_user_items.next())
							{
								itemData.read = results_user_items.getBoolean("read_state");
								itemData.starred = results_user_items.getBoolean("starred");
								lItemsData.add(itemData);
							}
							

						}
						data.items = new FeedItemsJSONData[lItemsData.size()];
						data.items = lItemsData.toArray(data.items);
						lData.add(data);
						
						lItemsData = new ArrayList<FeedItemsJSONData>();
			}
			
			if (i > 0)
			{
				
				response.data = new FeedJSONData[lData.size()];
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

	@GET
	@Secured	
	@Path("/{feed_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFeed(String s, @HeaderParam("Authorization") String header, @PathParam("feed_id") int feed_id)
	{
		ObjectMapper mapper = new ObjectMapper();
		FeedsGetResponse response = new FeedsGetResponse();
		String token = header.trim();
		
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
		
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "Select * from user_feed WHERE id_user = ? AND id_feed = ? ORDER BY id_feed");

			preparedStatement.setInt( 1, id_user );
			preparedStatement.setInt( 2, feed_id );
			ResultSet results = preparedStatement.executeQuery();
			
			
			FeedItemsJSONData		itemData = null;
			FeedJSONData			data = null;
			
			List<FeedJSONData>	lData = new ArrayList<FeedJSONData>();
			List<FeedItemsJSONData>	lItemsData = new ArrayList<FeedItemsJSONData>();
			
			int i = -1;
			while (results.next())
			{				
						i = results.getInt("id_feed");

						data = new FeedJSONData();
						data.id_feed = i;
						data.name = results.getString("name");
						
						PreparedStatement query_items = database.connection.prepareStatement( "Select * from items WHERE feed_id = ? ORDER BY id");
						
						query_items.setInt(1, i);
					
						ResultSet results_items = query_items.executeQuery();
						while (results_items.next())
						{
							itemData = null;
							itemData = new FeedItemsJSONData();
							
							itemData.id_item = results_items.getInt("id");
							itemData.id_feed = results_items.getInt("feed_id");
							itemData.title = results_items.getString("title");
							itemData.description = results_items.getString("description");
							itemData.link = results_items.getString("url");
							itemData.pubDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(results_items.getTimestamp("date"));
							
							PreparedStatement query = database.connection.prepareStatement( "Select * from user_items WHERE id_item = ? AND id_user = ? ORDER BY id");

							query.setInt( 1, results_items.getInt("id"));
							query.setInt( 2, id_user);
							ResultSet results_user_items = query.executeQuery();
							
							if (results_user_items.next())
							{
								itemData.read = results_user_items.getBoolean("read_state");
								itemData.starred = results_user_items.getBoolean("starred");
								lItemsData.add(itemData);
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

						}
						data.items = new FeedItemsJSONData[lItemsData.size()];
						data.items = lItemsData.toArray(data.items);
						lData.add(data);
						
						lItemsData = new ArrayList<FeedItemsJSONData>();
			}
			
			if (i > 0)
			{
				response.data = new FeedJSONData[lData.size()];
				response.data = lData.toArray(response.data);
			}
			else 
				throw new CustomNotFoundException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_DOESNT_EXIST));
			
			
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

	@DELETE
	@Secured	
	@Path("/{feed_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFeeds(String s, @HeaderParam("Authorization") String header, @PathParam("feed_id") int feed_id)
	{
		ObjectMapper mapper = new ObjectMapper();
		FeedDeleteResponse response = new FeedDeleteResponse();
		
		String token = header.trim();
		int id_user  = JSONUtils.getUserIdFromAuthorizationHeader(token);
		
		if (id_user == -1)
			throw new CustomInternalServerError(ErrorStrings.HEADER_PARSING_ERROR);
			
		DatabaseManager database = DatabaseManager.getInstance();
		try { 
			database.Connect();

			/* On vérifie si la catégorie est bien celle de l'utilisateur */
			PreparedStatement feed_state = database.connection.prepareStatement( "SELECT id FROM user_feed WHERE id_user = ? AND id_feed = ?");
			feed_state.setInt( 1, id_user );
			feed_state.setInt( 2, feed_id );
			
			ResultSet feed_results = feed_state.executeQuery();
			if (feed_results.next())
			{
				PreparedStatement feed_deletion = database.connection.prepareStatement( "DELETE FROM user_feed WHERE id_user = ? AND id_feed = ?");
				feed_deletion.setInt( 1, id_user );
				feed_deletion.setInt( 2, feed_id );
				
				int status = feed_deletion.executeUpdate();
				if (status == 0)
					throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_FEED_DELETION));
			}
			else
				throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_DOESNT_EXIST));
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

class FeedItemsJSONData
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

class FeedJSONData
{
	int 								id_feed;
	String 								name;
	//int 			unread;
	FeedItemsJSONData[]					items;
	
	public int getId_feed(){
		return (id_feed);
	}	
	
	public String getName(){
		return (name);
	}
	
	public FeedItemsJSONData[] getItems(){
		return (items);
	}
	
	public void setId_feed(int id)
	{
		id_feed = id;
	}
	
	public void setName(String _name)
	{
		name = _name;
	}
	
	public void setFeeds(FeedItemsJSONData[] _items)
	{
		items = _items;
	}
}

class FeedsGetResponse
{
	String								status;
	FeedJSONData[]				data;
	
	public String getStatus(){
		return (status);
	}
	
	public FeedJSONData[] getData(){
		return (data);
	}
	
	public void setStatus(String _status)
	{
		status = _status;
	}
	
	public void setData(FeedJSONData[] _data)
	{
		data = _data;
	}
	
	public String toString(){
	      return "CategoriesGetResponse [ status: "+ status +
	    		  				", data: "+ data + "]";
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

class FeedDeleteResponse
{
	String 	status;
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	public String getStatus() {
		return (this.status);
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