package org.typo3.solr.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoader {
	public static Logger log = LoggerFactory.getLogger(PropertiesLoader.class);

	public static Properties getProperties() {
		Properties properties = new Properties();
		String catalinaHome = System.getProperty("catalina.home");
		if(catalinaHome == null || catalinaHome.isEmpty()) {
			catalinaHome = "/opt/solr-tomcat/tomcat";
			log.debug("########## system property for catalina.home = null, set manuelly");
		}
		try {
			properties.load(new FileInputStream(new File(catalinaHome + "/conf/solr-typo3.properties")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return properties;
	}
}
