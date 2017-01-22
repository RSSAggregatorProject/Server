package com.rssaggregatorserver.entities;


public class Errors {

	String status;
	String error;
	
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
