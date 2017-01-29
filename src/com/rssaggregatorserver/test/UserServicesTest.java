package com.rssaggregatorserver.test;

import static org.junit.Assert.*;

import javax.print.attribute.standard.Media;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.rssaggregatorserver.services.UsersServices;

public class UserServicesTest extends JerseyTest {

private UsersServices services; 


@Override
protected Application  configure() {
	services = Mockito.mock(UsersServices.class);
	return new ResourceConfig(services.getClass());
}

@Test
public void postUserTest()
{
	String goodSyntax = "{\"email\" : \"toto\", \"password\" : \"jojo\"}";
	
	Mockito.doReturn(Response.created(null).build()).when(services).createUser(Mockito.anyString());

	
	final Entity<String> requestBody = Entity.entity(goodSyntax, MediaType.APPLICATION_JSON_TYPE);
	final Response test = target("/users").request(MediaType.APPLICATION_JSON_TYPE).post(requestBody);
	
	assertEquals(Response.Status.CREATED.getStatusCode(), test.getStatus());
	
}


}
