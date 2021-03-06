package com.rssaggregatorserver.entities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import org.glassfish.jersey.server.ManagedAsync;

import java.sql.Connection;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.enums.DatabaseStrings;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;

public class RSSTasks {
	
	
	static void fillNewFeedsInDatabase(SyndEntry entry, int feed_id)
	{
		
		
		DatabaseManager database = DatabaseManager.getInstance();
		
		String title = entry.getTitle();
		String url = entry.getLink();
		Date date = entry.getPublishedDate();
		String desc = entry.getDescription().getValue();
		
		System.out.println("Adding Feeds : " + entry.getTitle() + " " + entry.getPublishedDate());
		
		Connection connexion = null;
		
		try { 
		
			//database.Connect();
			connexion = DriverManager.getConnection(DatabaseStrings.DATABASE_URL, DatabaseStrings.DATABASE_USER, DatabaseStrings.DATABASE_PASS);
			
			PreparedStatement preparedStatement = connexion.prepareStatement( "SELECT * FROM items WHERE url = ?");
			
			preparedStatement.setString(1, url);
			
			ResultSet results = preparedStatement.executeQuery();
			
			List<Integer>	iDs = new ArrayList<Integer>();
			
			while (results.next())
				iDs.add(results.getInt("id"));
			
			if (iDs.size() > 0)
			{
				try { connexion.close(); } catch (SQLException e1) {}
				System.out.println("Connection Close ! Already Exist !");
				return ;
			}
			
			preparedStatement.close();
			preparedStatement = connexion.prepareStatement( "INSERT INTO items (title, description, date, url, feed_id) VALUES (?, ?, ?, ?, ?)");
			preparedStatement.setString(1, title);
			preparedStatement.setString(2, desc);
			
			Timestamp ts = new Timestamp(date.getTime());
			preparedStatement.setString(3, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(ts));
			preparedStatement.setString(4, url);
			preparedStatement.setInt(5, feed_id);
			int status = preparedStatement.executeUpdate();
			preparedStatement.close();
			results.close();
			
			if (status == 0)
				throw new CustomInternalServerError(Errors.createJSONErrorResponse(ErrorStrings.DATABASE_ERROR_REGISTER_FEEDS));
			
		}
		catch (SQLException e) {try { connexion.close(); } catch (SQLException ignore){ } throw new CustomInternalServerError(e.getMessage()); }
		finally 
		{
			try { connexion.close(); } catch (SQLException e1) {}
			try {
				if (connexion.isClosed())
					System.out.println("Connection Close !");
			} catch (SQLException e) { System.out.println("Not close !"); }
		}
	}
	
	
	public static String	getFeedName(String str)
	{
		String tmp = "";
		
		try {
			URL url = new URL(str);
			XmlReader xmlReader = null;

			
				try {
					xmlReader = new XmlReader(url);
					SyndFeed feeder = new SyndFeedInput().build(xmlReader);
					
					tmp = feeder.getTitle();
					System.out.println(" < " + tmp + ">");
					
				} catch (IOException | IllegalArgumentException | FeedException e) { } finally {
					if (xmlReader != null)
					 try { xmlReader.close();} catch (IOException ignore) {}
					else if (xmlReader == null)
						return (null);
				}
				
		} catch (MalformedURLException e) {
			throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_URI_MISSING));
		}
		
		return (tmp);
	}
	
	public static int 	  	readFeeds(String str, int feed_id)
	{
		
		try {
			URL url = new URL(str);
			XmlReader xmlReader = null;

			
				try {
					xmlReader = new XmlReader(url);
					SyndFeed feeder = new SyndFeedInput().build(xmlReader);
					
				      for (Iterator iterator = feeder.getEntries().iterator(); iterator
				              .hasNext();) 
				      {
				          SyndEntry syndEntry = (SyndEntry) iterator.next();
				          fillNewFeedsInDatabase(syndEntry, feed_id);
				            
				      }
					
				} catch (IOException | IllegalArgumentException | FeedException e) { } finally {
					if (xmlReader != null)
					 try { xmlReader.close();} catch (IOException ignore) {}
					else if (xmlReader == null)
						return (-1);
				}
				
		} catch (MalformedURLException e) {
			throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_URI_MISSING));
		}
		
		
		return (0);
	}
	
	public static boolean readFeedUrl(String str)
	{
		try {
			URL url = new URL(str);
			XmlReader xmlReader = null;
			
				try {
					xmlReader = new XmlReader(url);
					SyndFeed feeder = new SyndFeedInput().build(xmlReader);
					
				} catch (IOException | IllegalArgumentException | FeedException e) { } finally {
					if (xmlReader != null)
					 try { xmlReader.close();} catch (IOException ignore) {}
					else if (xmlReader == null)
						return (false);
				}
				
		} catch (MalformedURLException e) {
			throw new CustomBadRequestException(Errors.createJSONErrorResponse(ErrorStrings.REQUEST_FEEDS_URI_MISSING));
		}
			
		return (true);
	}
	
	
}
