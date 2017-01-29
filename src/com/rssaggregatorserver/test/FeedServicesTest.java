package com.rssaggregatorserver.test;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.mockito.Mockito;

import com.rssaggregatorserver.services.CategoriesServices;
import com.rssaggregatorserver.services.FeedServices;

public class FeedServicesTest extends JerseyTest {

	private FeedServices services; 


	@Override
	protected Application  configure() {
		services = Mockito.mock(FeedServices.class);
		return new ResourceConfig(services.getClass());
	}

	@Test
	public void getFeedTest()
	{
		
		Mockito.doReturn(Response.ok().build()).when(services).getFeeds(Mockito.anyString(), Mockito.anyString());

		
		//final Entity<String> requestBody = Entity.entity(goodSyntax, MediaType.APPLICATION_JSON_TYPE);
		final Response test = target("/feeds").request().get();
		
		assertEquals(Response.Status.OK.getStatusCode(), test.getStatus());
		
	}
	
	@Test
	public void getFeedStarredTest()
	{
		
		Mockito.doReturn(Response.ok().build()).when(services).getFeedsStarred(Mockito.anyString(), Mockito.anyString());

		
		//final Entity<String> requestBody = Entity.entity(goodSyntax, MediaType.APPLICATION_JSON_TYPE);
		final Response test = target("/feeds/starred").request().get();
		
		assertEquals(Response.Status.OK.getStatusCode(), test.getStatus());
		
	}
}
