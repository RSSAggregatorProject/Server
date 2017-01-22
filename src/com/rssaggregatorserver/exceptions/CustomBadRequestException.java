package com.rssaggregatorserver.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.BadRequestException;

@Provider
public class CustomBadRequestException extends WebApplicationException {
  public CustomBadRequestException(String message) {
    super(Response.status(400).entity(message).type(MediaType.APPLICATION_JSON).build());
  }
}
