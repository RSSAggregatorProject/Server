package com.rssaggregatorserver.entities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Errors {

	String status;
	String error;
	
	public static String createJSONErrorResponse(String message)
	{
		String tmp = "";
		Errors error = new Errors(message);
		System.out.println(error.toString());
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			tmp = mapper.writeValueAsString(error);
		} catch (JsonProcessingException e) {
			tmp = "{ \"status\":\"error\", \"error\":\"An error has occured !\"}";
		}
		
		return (tmp);
	}
	
	public Errors(String message)
	{
		this.status = "error";
		this.error = message;
	}
	
	public String getError()
	{
		return (this.error);
	}
	
	public String getStatus()
	{
		return (this.status);
	}
	
	public void setStatus(String _status)
	{
		this.status = _status; 
	}
	
	public void setError(String message)
	{
		this.error = message;
	}
	
	public String toString()
	{
		return ("Errors : [ status: " + this.status + ", error : " + this.error + " ]");
	}
}
