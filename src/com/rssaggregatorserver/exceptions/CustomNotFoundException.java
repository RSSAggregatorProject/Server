package com.rssaggregatorserver.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.BadRequestException;

public class CustomNotFoundException extends WebApplicationException {
	  public CustomNotFoundException(String message) {
		    super(Response.status(404).entity(message).type(MediaType.APPLICATION_JSON).build());
		  }
}