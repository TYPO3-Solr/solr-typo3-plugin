package org.typo3.solr.handler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.UpdateRequestHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import org.apache.solr.search.SolrIndexSearcher;
import org.typo3.solr.cache.KittyCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.typo3.solr.common.PropertiesLoader;


public class ControlUpdateHandler extends UpdateRequestHandler{
	public static Logger log = LoggerFactory.getLogger(ControlUpdateHandler.class);

	private final String DOCUMENT_LIMIT_KEY = "document_limit";
	private final String NUM_DOCS_KEY = "num_docs";

	private KittyCache cache = null;
	Properties properties = null;
	// time to live in secods
	int timeToLive;
	// maximal number of objects to cache
	int maxSize;

	@SuppressWarnings("rawtypes")
	public void init(NamedList args) {
		properties = PropertiesLoader.getProperties();
		maxSize = getIntProperty(properties.getProperty("sorl.cache.maxNumber"), 7);
		timeToLive = getIntProperty(properties.getProperty("sorl.cache.timeToLive"), 60);
		if(cache == null) {
			cache = new KittyCache(maxSize);
		}
		super.init(args);
	}


	@SuppressWarnings("unchecked")
	public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
		String internal_name = getInternalName(req);;
		int document_limit_cache = 0;
		int num_docs_cache = 0;

		if(cache.size() == 0 || (cache.queueSize() == maxSize)) {
			if(cache.queueSize() == maxSize) {
				cache.clear();
			}

			document_limit_cache = getDocumentLimit(internal_name);
			if(document_limit_cache > 0) {
				num_docs_cache = getNumDocs(req.getSearcher());
			}

			cache.put(DOCUMENT_LIMIT_KEY,document_limit_cache, timeToLive);
			cache.put(NUM_DOCS_KEY,num_docs_cache, timeToLive);
		}

		if(cache.queueSize() < maxSize) {
			cache.put(System.currentTimeMillis(), System.currentTimeMillis(), timeToLive);
		}

		if(cache.get(DOCUMENT_LIMIT_KEY) == null) {
			document_limit_cache = getDocumentLimit(internal_name);
			cache.put(DOCUMENT_LIMIT_KEY,document_limit_cache, timeToLive);
		}

		if(cache.get(NUM_DOCS_KEY) == null) {
			num_docs_cache = getNumDocs(req.getSearcher());
			cache.put(NUM_DOCS_KEY,num_docs_cache, timeToLive);
		}

		int document_limit = Integer.parseInt(cache.get(DOCUMENT_LIMIT_KEY).toString());
		int numDocs = Integer.parseInt(cache.get(NUM_DOCS_KEY).toString());

		System.out.println("######### document_limit: " + document_limit);
		System.out.println("######### num_docs_cache: " + numDocs);

		if(numDocs <= document_limit) {
			super.handleRequestBody(req, rsp);
		}
		else {
			String solrError = properties.getProperty("solr.error.maxDocs");
			if(StringUtils.isEmpty(solrError)) {
				solrError = "You have reached the maximum number of documents";
			}
			solrError = solrError + " (numDocs " + numDocs + " / limit " + document_limit + ") for " + internal_name;
			cache.clear();
			throw new SolrException(SolrException.ErrorCode.FORBIDDEN, solrError);
		}
	 }


	private String getInternalName(SolrQueryRequest req) {
		String internal_name = "";
		SolrCore core = req.getCore();
		SolrResourceLoader loader = core.getResourceLoader();

		String instanceDir = loader.getInstanceDir();
		String solrpath = properties.getProperty("solr.path");
		if(StringUtils.isEmpty(solrpath)) {
			solrpath = "/opt/solr-tomcat/solr/";
		}
		instanceDir = instanceDir.substring(solrpath.length());
		String[] folder = instanceDir.split("/");
		if(folder.length == 2) {
			internal_name = folder[0];
		}
		return internal_name;
	}

	private int getDocumentLimit(String internal_name) throws Exception {
		int document_limit = 0;
		if(!StringUtils.isEmpty(internal_name)) {
			String query = "SELECT p.hard_document_limit FROM pricings p, solr_cores s " +
					"WHERE s.`internal_name` = '" + internal_name + "' " +
					"AND s.`pricing_id` = p.`id`";

			Context envContext = new InitialContext();
			DataSource datasource = (DataSource)envContext.lookup("java:/comp/env/jdbc/hostedSolr");
			Connection connection = datasource.getConnection();
			Statement statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if(result != null) {
				ResultSetMetaData resultMeta = result.getMetaData();
			    int numberOfColumns = resultMeta.getColumnCount();
			    if(numberOfColumns == 1) {
			    	result.next();
			    	document_limit = result.getInt("hard_document_limit");
			    }
			}

			result.close();
			statement.close();
			connection.close();
		}
		return document_limit;
	}

	private int getNumDocs(SolrIndexSearcher searcher) {
		int numDocs = 0;
		//SolrIndexReader reader = searcher.getReader();

		DirectoryReader reader = searcher.getIndexReader();

		if(reader != null) {
			numDocs = reader.numDocs();
		}
		System.out.println("######### numDocs: " + numDocs);
		return numDocs;
	}

	public int getIntProperty(String property, int value) {
		int returnValue = 0;
		if(!StringUtils.isEmpty(property)) {
			returnValue = Integer.parseInt(property);
		}
		else {
			returnValue = value;
		}
		return returnValue;
	}
}
