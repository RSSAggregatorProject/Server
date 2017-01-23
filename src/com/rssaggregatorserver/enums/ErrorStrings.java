package com.rssaggregatorserver.enums;

public class ErrorStrings {
	
	public static final String MISSING_EMAIL = "Email is missing !";
	public static final String MISSING_PASS = "Password is missing !";
	public static final String EMAIL_ALREADY_USED = "Email is already used !";
	
	public static final String AUTH_NOT_PROVIDE = "Authorization header must be provide !";
	public static final String AUTH_BAD_TOKEN = "Your token is not a valid one !";
	public static final String AUTH_EXP_DATE = "Your token has expired !";
	
	public static final String CRITICAL_ERROR = "Critical error !";
	public static final String UNKNOWN_USER = "Wrong email or password !";
	
	public static final String DATABASE_ERROR_TOKEN = "Cannot create token !";
	public static final String DATABASE_ERROR_REGISTRATION = "Something append during registration !";
	
	public static final String REQUEST_FORMAT_INVALID = "Request format is invalid !";

}
