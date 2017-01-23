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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
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
			 mapper.enable(SerializationFeature.INDENT_OUTPUT);
			 System.out.println(request);
				
		} catch (JsonParseException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (JsonMappingException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		catch (IOException e) {throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FORMAT_INVALID)); }
		
		
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