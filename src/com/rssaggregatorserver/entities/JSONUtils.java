package com.rssaggregatorserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtils {
	
	public static String createJSONResponse(Object response)
	{
		String tmp = "";
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			tmp = mapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			tmp = "{ \"status\":\"error\", \"error\":\"Error parsing auth reponse !\"}";
		}
		
		return (tmp);
	}
}
