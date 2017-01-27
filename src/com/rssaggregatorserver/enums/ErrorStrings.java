package com.rssaggregatorserver.enums;

public class ErrorStrings {
	
	public static final String MISSING_EMAIL = "Email is missing !";
	public static final String MISSING_PASS = "Password is missing !";
	public static final String MISSING_NAME = "Name is missing !";
	
	public static final String EMAIL_ALREADY_USED = "Email is already used !";
	
	public static final String AUTH_NOT_PROVIDE = "Authorization header must be provide !";
	public static final String AUTH_BAD_TOKEN = "Your token is not a valid one !";
	public static final String AUTH_EXP_DATE = "Your token has expired !";
	
	public static final String CRITICAL_ERROR = "Critical error !";
	public static final String HEADER_PARSING_ERROR = "Server cannot gathering user information !";
	public static final String UNKNOWN_USER = "Wrong email or password !";
	
	public static final String DATABASE_ERROR_TOKEN = "Cannot create token !";
	public static final String DATABASE_ERROR_FEED_DELETION = "Something append during deletion !";
	public static final String DATABASE_ERROR_REGISTRATION = "Something append during registration !";
	public static final String DATABASE_ERROR_REGISTER_CATEGORIES = "An error occured during category creation !";
	public static final String DATABASE_ERROR_REGISTER_FEEDS = "An error occured during feed creation !";
	public static final String DATABASE_ERROR_REGISTER_ITEMS = "An error occured during item creation !";
	
	public static final String REQUEST_FORMAT_INVALID = "Request format is invalid !";
	
	public static final String REQUEST_FEEDS_CAT_DOESNT_EXIST = "Users's categorie doesn't exist !";
	public static final String REQUEST_FEEDS_DOESNT_EXIST = "Users's feed doesn't exist !";
	public static final String REQUEST_FEEDS_URI_MISSING = "You must specify feed's uri !";
	
	public static final String REQUEST_FEEDS_NAME_MISSING = "An empty name for a feed is not allowed !";
	public static final String REQUEST_FEEDS_URI_INCORRECT = "Uri is invalid !";

}
