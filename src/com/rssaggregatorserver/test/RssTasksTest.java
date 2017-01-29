package com.rssaggregatorserver.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.rssaggregatorserver.entities.RSSTasks;

public class RssTasksTest {
	
	@Test
	public void getFeedNameTest()
	{
		String tmp = RSSTasks.getFeedName("http://www.eurosport.fr/rss.xml");
		
		assertEquals("Eurosport - Top des infos", tmp);
	}
	
	@Test
	public void readFeedUrl()
	{	
		assertTrue(RSSTasks.readFeedUrl("http://www.eurosport.fr/rss.xml"));
	}
}
