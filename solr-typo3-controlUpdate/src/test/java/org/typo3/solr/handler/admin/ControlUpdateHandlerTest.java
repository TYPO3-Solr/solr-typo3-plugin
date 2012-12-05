package org.typo3.solr.handler.admin;

//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;

import org.apache.solr.SolrTestCaseJ4;
//import org.apache.solr.core.SolrCore;
//import org.apache.solr.request.LocalSolrQueryRequest;
//import org.apache.solr.response.SolrQueryResponse;
//import org.apache.solr.common.params.MapSolrParams;
//import org.apache.solr.common.util.ContentStream;
//import org.apache.solr.common.util.ContentStreamBase;
//import org.apache.solr.common.util.NamedList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.typo3.solr.handler.ControlUpdateHandler;

public class ControlUpdateHandlerTest extends SolrTestCaseJ4 {

	ControlUpdateHandler handler;

	@BeforeClass
	public static void beforeClass() throws Exception {
		initCore("solrconfig.xml", "schema.xml", System.getProperty("solr.solr.home"));
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	    handler = new ControlUpdateHandler();
	}

	public void testReadIndexDirecotry() throws Exception {
//		SolrCore core = h.getCore();
//		assertEquals("95748a1c551-test-pd/conf", core.getco);
	}

	public void testUpdateRequestWithXml() throws Exception {
//		String xml = "" +
//				"<add>" +
//				"	<doc>" +
//				"		<field name=\"id\">1</field>" +
//				"		<field name=\"type\">pages</field>" +
//				"		<field name=\"appKey\">foo</field>" +
//				"	</doc>" +
//				"</add>";
//		Map<String,String> args = new HashMap<String, String>();
//		args.put("commit", "true");
//		SolrCore core = h.getCore();
//		LocalSolrQueryRequest req = new LocalSolrQueryRequest( core, new MapSolrParams( args) );
//		ArrayList<ContentStream> streams = new ArrayList<ContentStream>();
//		streams.add(new ContentStreamBase.StringStream(xml));
//		req.setContentStreams(streams);
//
//		SolrQueryResponse rsp = new SolrQueryResponse();
//		handler.init(new NamedList<String>());
//		handler.setConfigurationDirectory(core);
//		File configDir = handler.getConfigurationDirectory();
//		handler.handleRequestBody(req, rsp);
	}
}
