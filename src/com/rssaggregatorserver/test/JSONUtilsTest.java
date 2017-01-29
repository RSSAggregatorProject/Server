package com.rssaggregatorserver.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.rssaggregatorserver.entities.JSONUtils;

public class JSONUtilsTest {

	
	@Test
	public void createJSONResponseTest()
	{
		System.out.println(JSONUtils.createJSONResponse("toto"));
		assertEquals("\"toto\"", JSONUtils.createJSONResponse("toto"));
		
	}
	
	@Test
	public void getUserIdFromAuthorizationHeaderTest()
	{
		assertEquals(1, JSONUtils.getUserIdFromAuthorizationHeader("MTExODIvMS90b3RvQHR1dGF0YS5mci8zMDU5"));
	}
	
	
}
