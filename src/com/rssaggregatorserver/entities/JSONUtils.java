package com.rssaggregatorserver.entities;

import java.io.UnsupportedEncodingException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;

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
	
	public static int getUserIdFromAuthorizationHeader(String tokenHeader)
	{
		String tmp;
		int id = -1;
		try {
			tmp = TokenEncode.Decode(tokenHeader);
			System.out.println(tmp);
			
			String[] userInfos = tmp.split("/");
			System.out.println(userInfos[1]);
			id = Integer.parseInt(userInfos[1]);
			
		} catch (UnsupportedEncodingException e) {
			throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.HEADER_PARSING_ERROR));
		}
		
		
		
		return (id);
	}
}
