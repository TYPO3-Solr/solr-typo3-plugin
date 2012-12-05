package org.typo3.solr.handler.admin;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.junit.BeforeClass;

/**
 * Test class to test the Configuration Request Handler
 *
 * @author Denis Sinner <denis.sinner@dkd.de>
 */
public class ConfigurationHandlerTest extends SolrTestCaseJ4 {
  ConfigurationHandler configHandler;

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();

//  configHandler = (ConfigurationHandler)h.getCore().getRequestHandler("/configuration");
    configHandler = new ConfigurationHandler();
    configHandler.initConfigurationFiles();
    configHandler.inform(h.getCore());
  }

  @Override
  public void tearDown() throws Exception {
    SolrResourceLoader loader = h.getCore().getResourceLoader();

    File stopWords = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("stopWords"));
    File protWords = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("protectedWords"));
    File synonyms = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("synonyms"));
    File elevate = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("contentElevation"));

    FileUtils.writeStringToFile(stopWords, "");
    FileUtils.writeStringToFile(protWords, "");
    FileUtils.writeStringToFile(synonyms, "");
    FileUtils.writeStringToFile(elevate, "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> <elevate />");

    super.tearDown();
  }

  //tests
  public void testWriteRequestStopWords() throws Exception {
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestStopWords =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "stopWords");
    assertNotNull("false request class for stopWords", requestStopWords);
    requestStopWords.setContentStreams(createContentStream("me\nyou\ntest"));

    try {
      configHandler.handleRequestBody(requestStopWords, resp);
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());

    // check if content was written to file
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File result = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("stopWords"));

    assertEquals("Content written doesn't fit expected result", "me\nyou\ntest", FileUtils.readFileToString(result));
  }

  public void testWriteRequestProtWords() throws Exception {
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestProtWords =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "protectedWords");
    assertNotNull("false request class for protectedWords", requestProtWords);
    requestProtWords.setContentStreams(createContentStream("TYPO3"));

    try {
      configHandler.handleRequestBody(requestProtWords, resp);
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());

    // check if content was written to file
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File result = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("protectedWords"));

    assertEquals("Content written doesn't fit expected result", "TYPO3", FileUtils.readFileToString(result));
  }

  public void testWriteRequestSynonyms() throws Exception {
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestSynonyms =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "synonyms");
    assertNotNull("false request class for synonyms", requestSynonyms);
    requestSynonyms.setContentStreams(createContentStream("aaa => aaaa\na\\=>a => b\\=>b\nfooaaa,baraaa,bazaaa"));

    try {
      configHandler.handleRequestBody(requestSynonyms, resp);
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());

    // check if content was written to file
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File result = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("synonyms"));

    assertEquals("Content written doesn't fit expected result",
        "aaa => aaaa\na\\=>a => b\\=>b\nfooaaa,baraaa,bazaaa",
        FileUtils.readFileToString(result));
  }

  public void testWriteRequestElevate() throws Exception {
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestElevate =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "contentElevation");
    assertNotNull("false request class for elevate", requestElevate);
    requestElevate.setContentStreams(createContentStream(
        "<elevate><query text=\"foo bar\"><doc id=\"1\" /><doc id=\"2\" /><doc id=\"3\" /></query></elevate>"));

    try {
      configHandler.handleRequestBody(requestElevate, resp);
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }

    // check if content was written to file
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File result = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("contentElevation"));

    assertEquals("Content written doesn't fit expected result",
        "<elevate><query text=\"foo bar\"><doc id=\"1\" /><doc id=\"2\" /><doc id=\"3\" /></query></elevate>",
        FileUtils.readFileToString(result));
  }

  public void testReadRequestStopWords() throws Exception {
    // prepare content for read test
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File contentFile = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("stopWords"));

    FileUtils.writeStringToFile(contentFile, "me\nyou\ntest");

    // read test
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestStopWords =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "stopWords");
    assertNotNull("false request class for stopWords", requestStopWords);

    try {
      configHandler.handleRequestBody(requestStopWords, resp);
      String value = (String)resp.getValues().get(configHandler.getConfigurationFiles().get("stopWords"));
      assertNotNull(value);
      assertEquals(value, "me\nyou\ntest");
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());
  }

  public void testReadRequestProtWords() throws Exception {
    // prepare content for read test
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File contentFile = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("protectedWords"));

    FileUtils.writeStringToFile(contentFile, "TYPO3");

    // read test
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestProtWords =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "protectedWords");
    assertNotNull("false request class for stopWords", requestProtWords);

    try {
      configHandler.handleRequestBody(requestProtWords, resp);
      String value = (String)resp.getValues().get(configHandler.getConfigurationFiles().get("protectedWords"));
      assertNotNull(value);
      assertEquals(value, "TYPO3");
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());
  }

  public void testReadRequestSynonyms() throws Exception {
    // prepare content for read test
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File contentFile = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("synonyms"));

    FileUtils.writeStringToFile(contentFile, "aaa => aaaa\na\\=>a => b\\=>b\nfooaaa,baraaa,bazaaa");

    // read test
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestSynonyms =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "synonyms");
    assertNotNull("false request class for stopWords", requestSynonyms);

    try {
      configHandler.handleRequestBody(requestSynonyms, resp);
      String value = (String)resp.getValues().get(configHandler.getConfigurationFiles().get("synonyms"));
      assertNotNull(value);
      assertEquals(value, "aaa => aaaa\na\\=>a => b\\=>b\nfooaaa,baraaa,bazaaa");
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());
  }

  public void testReadRequestElevate() throws Exception {
    // prepare content for read test
    SolrResourceLoader loader = h.getCore().getResourceLoader();
    File contentFile = new File(loader.getConfigDir() + configHandler.getConfigurationFiles().get("contentElevation"));

    FileUtils.writeStringToFile(contentFile,
        "<elevate><query text=\"foo bar\"><doc id=\"1\" /><doc id=\"2\" /><doc id=\"3\" /></query></elevate>");

    // read test
    SolrQueryResponse resp = new SolrQueryResponse();
    LocalSolrQueryRequest requestElevate =
        (LocalSolrQueryRequest)req(ConfigurationHandler.CONFIGURATION_TYPE_PARAMETER_NAME, "contentElevation");
    assertNotNull("false request class for stopWords", requestElevate);

    try {
      configHandler.handleRequestBody(requestElevate, resp);
      String value = (String)resp.getValues().get(configHandler.getConfigurationFiles().get("contentElevation"));
      assertNotNull(value);
      assertEquals(value, "<elevate><query text=\"foo bar\"><doc id=\"1\" /><doc id=\"2\" /><doc id=\"3\" /></query></elevate>");
    } catch(Exception e) {
      fail("Exception on ConfigurationHandler.handleRequestBody");
    }
    assertNull("Exception in response", resp.getException());
  }

  protected Iterable<ContentStream> createContentStream(String content) {
    ArrayList<ContentStream> contentStreams = new ArrayList<ContentStream>();
    ContentStreamBase.StringStream stream = new ContentStreamBase.StringStream(content);
    contentStreams.add(stream);
    return contentStreams;
  }
}
