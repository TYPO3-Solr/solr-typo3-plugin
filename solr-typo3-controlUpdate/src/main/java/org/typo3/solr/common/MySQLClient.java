package org.typo3.solr.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySQLClient {
	public static Logger log = LoggerFactory.getLogger(MySQLClient.class);

	private static MySQLClient mySqlConnector = null;
	private Connection connection = null;

	private MySQLClient() throws InstantiationException, IllegalAccessException {
		Properties properties = new Properties();
		try {
			properties = PropertiesLoader.getProperties();

		    String host = properties.getProperty("host");
		    int port 	= new Integer(properties.getProperty("port")).intValue();
		    String database = properties.getProperty("database");
		    String username = properties.getProperty("username");
		    String password = properties.getProperty("password");

		    Class.forName("com.mysql.jdbc.Driver").newInstance();
		    this.connection = DriverManager.getConnection("jdbc:mysql://" + host +":" +  port + "/" + database,
		    													username, password);
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}

	public static synchronized MySQLClient getInstance() throws Exception{
		if(mySqlConnector == null) {
			mySqlConnector = new MySQLClient();
		}
		return mySqlConnector;
	}

	public ResultSet query(String query) throws SQLException{
		 ResultSet result = null;
		 if(connection != null && !query.isEmpty()) {
			 Statement statement = connection.createStatement();
			 result = statement.executeQuery(query);
		 }
		return result;
    }

	public void close() {
		if(connection != null) {
			try {
				connection.close();
			}
			catch(SQLException e) {

			}
			connection = null;
		}
	}

}
