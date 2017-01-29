package com.rssaggregatorserver.test;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.Mockito;

import com.rssaggregatorserver.services.CategoriesServices;

public class CategoriesServicesTest extends JerseyTest {
	
	private CategoriesServices services; 


	@Override
	protected Application  configure() {
		services = Mockito.mock(CategoriesServices.class);
		return new ResourceConfig(services.getClass());
	}

	@Test
	public void getCategorieTest()
	{
		
		Mockito.doReturn(Response.ok().build()).when(services).getCategories(Mockito.anyString(), Mockito.anyString());

		
		//final Entity<String> requestBody = Entity.entity(goodSyntax, MediaType.APPLICATION_JSON_TYPE);
		final Response test = target("/categories").request().get();
		
		assertEquals(Response.Status.OK.getStatusCode(), test.getStatus());
		
	}
}
