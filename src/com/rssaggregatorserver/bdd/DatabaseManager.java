package com.rssaggregatorserver.bdd;


import java.sql.SQLException;

import java.sql.Connection;
import java.sql.DriverManager;

import com.rssaggregatorserver.enums.DatabaseStrings;

public class DatabaseManager {
	
	public Connection connection;
	
	private DatabaseManager() 
	{
		try {
		    Class.forName( "com.mysql.jdbc.Driver" );
		} catch ( ClassNotFoundException e ) {
		}
	}
	
	private static DatabaseManager INSTANCE = null;
	
	public static DatabaseManager getInstance()
	{
		if (INSTANCE == null)
			INSTANCE = new DatabaseManager();
		
		return INSTANCE;
	}

	public void Disconnect()
	{
		if (connection != null)
		{
			try {
				connection.close();
			} catch (SQLException ignore) {}
		}
	}
	
	public void Connect() throws SQLException
	{
		String url = DatabaseStrings.DATABASE_URL;
		String user = DatabaseStrings.DATABASE_USER;
		String pass = DatabaseStrings.DATABASE_PASS;
		
		Disconnect();
		connection = DriverManager.getConnection(url, user, pass);
	}
}
