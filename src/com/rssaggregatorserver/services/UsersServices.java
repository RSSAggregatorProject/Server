package com.rssaggregatorserver.services;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/users")
public class UsersServices {
	
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String getUsers()
	{
		String str = "Test de motherfucker !";
		return (str);
	}

}
