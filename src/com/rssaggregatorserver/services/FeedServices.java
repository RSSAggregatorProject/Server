package com.rssaggregatorserver.services;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rssaggregatorserver.filters.Secured;

@Path("/feeds")
public class FeedServices {

	@POST
	@Secured	
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postFeeds(String s)
	{
		ObjectMapper mapper = new ObjectMapper();
		FeedPostRequest request = null;
		try {
			 request = mapper.readValue(s, FeedPostRequest.class);
				
				
		} catch (JsonParseException e) {e.printStackTrace(); }
		catch (JsonMappingException e) {e.printStackTrace(); }
		catch (IOException e) {e.printStackTrace(); }
		
		
		return (null);
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