package com.rssaggregatorserver.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class CustomInternalServerError extends WebApplicationException {
	  public CustomInternalServerError(String message) {
		    super(Response.status(500).entity(message).type(MediaType.APPLICATION_JSON).build());
		  }
}
