package com.rssaggregatorserver.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CustomNotAuthorizedException extends WebApplicationException {
	  public CustomNotAuthorizedException(String message) {
		    super(Response.status(401).entity(message).header("Access-Control-Allow-Origin", "*").type(MediaType.APPLICATION_JSON).build());
		  }

}
