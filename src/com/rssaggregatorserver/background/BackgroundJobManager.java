package com.rssaggregatorserver.background;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.mysql.jdbc.Statement;
import com.rssaggregatorserver.bdd.DatabaseManager;
import com.rssaggregatorserver.entities.Errors;
import com.rssaggregatorserver.entities.JSONUtils;
import com.rssaggregatorserver.entities.RSSTasks;
import com.rssaggregatorserver.enums.ErrorStrings;
import com.rssaggregatorserver.exceptions.CustomBadRequestException;
import com.rssaggregatorserver.exceptions.CustomInternalServerError;

public class BackgroundJobManager implements Job {

    @Override
    public void execute(final JobExecutionContext ctx)
            throws JobExecutionException {

		DatabaseManager database = DatabaseManager.getInstance();
    	try {

			database.Connect();

			PreparedStatement preparedStatement = database.connection.prepareStatement( "SELECT * FROM feeds");
			
			/* Exécution de la requête */
			ResultSet result = preparedStatement.executeQuery();
					
			while (result.next())
			{
				if (!RSSTasks.readFeedUrl(result.getString("feed_link")))
					return ;
				else
					RSSTasks.readFeeds(result.getString("feed_link"), result.getInt("id"));
			}

    	}
		catch (SQLException e) {
			// TODO Auto-generated catch block
		}
		finally
		{
			database.Disconnect();
		}

    }

}
