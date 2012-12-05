/**
 * Copyright 2011 Ingo Renner <ingo@typo3.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typo3.solr.handler.admin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.SolrCoreAware;



/**
 * Configuration update request handler to update synonyms, protected words,
 * stop words, and content elevation configuration from within TYPO3.
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public class ConfigurationHandler extends RequestHandlerBase implements SolrCoreAware {

  private final Map<String, String> configurationFiles = new HashMap<String, String>();
  private ConfigurationHandlerMode mode = ConfigurationHandlerMode.READ;
  private File configurationDirectory;
  private String configurationType;

  /**
   * GET parameter name to specify which configuration file to work on.
   */
  public static final String CONFIGURATION_TYPE_PARAMETER_NAME = "type";

  /**
   * Configuration update request handler version.
   */
  public static final String version = "1.0.0-dev";

  @SuppressWarnings("rawtypes")
  @Override
  public void init(NamedList args) {
    super.init(args);

    initConfigurationFiles();
  }

  public void initConfigurationFiles() {
    configurationFiles.put("synonyms", "synonyms.txt");
    configurationFiles.put("protectedWords", "protwords.txt");
    configurationFiles.put("stopWords", "stopwords.txt");
    configurationFiles.put("contentElevation", "elevate.xml");
  }

  public Map<String, String> getConfigurationFiles() {
    return configurationFiles;
  }


  /**
   * Detects whether we're in read or write mode and sends the request off to
   * the according specific handler method.
   *
   * @param req Solr request
   * @param rsp Solr response
   * @throws Exception if configuration type parameter is invalid or missing
   */
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    SolrParams params = req.getParams();

    Iterable<ContentStream> streams = req.getContentStreams();
    if (null != streams) {
    	mode = ConfigurationHandlerMode.WRITE;
    }
    else {
    	mode = ConfigurationHandlerMode.READ;
    }


    configurationType = params.get(CONFIGURATION_TYPE_PARAMETER_NAME);
    if (null == configurationType || !configurationFiles.containsKey(configurationType)) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "missing or invalid configuration type");
    }

    if (mode == ConfigurationHandlerMode.WRITE) {
      handleWriteRequest(req, rsp);
    } else {
      handleReadRequest(req, rsp);
    }
  }

  /**
   * Handles read requests, reads the requested file and provides it in the
   * response.
   *
   * @param req Solr request
   * @param rsp Solr response
   */
  private void handleReadRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
    File configurationFile = new File(configurationDirectory, configurationFiles.get(configurationType));
    StringBuffer fileContent = new StringBuffer();

    try {
      FileReader reader = new FileReader(configurationFile);
      BufferedReader input = new BufferedReader(reader);
      boolean firstLine = true;

      try {
        String line = null;

        while ((line = input.readLine()) != null) {
          if(firstLine) {
            firstLine = false;
          } else {
            fileContent.append("\n");
          }
          fileContent.append(line);
        }
      } finally {
        input.close();
        reader.close();
      }
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e.toString());
    }

    rsp.add(configurationFiles.get(configurationType), fileContent.toString());
  }

  /**
   * Handles write requests, takes the submitted configuration and writes it to
   * the according configuration file.
   *
   * @param req Solr request
   * @param rsp Solr response
   */
  private void handleWriteRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
    Iterable<ContentStream> streams = req.getContentStreams();
    ContentStream stream = streams.iterator().next();

    File configurationFile = new File(configurationDirectory, configurationFiles.get(configurationType));

    try {
      FileWriter writer = new FileWriter(configurationFile);
      BufferedWriter output = new BufferedWriter(writer);
      BufferedReader input = new BufferedReader(stream.getReader());
      boolean firstLine = true;

      try {
        String line = null;
        while((line = input.readLine()) != null) {
          if(firstLine) {
            firstLine = false;
          } else {
            output.newLine();
          }
          output.append(line);
        }
      } finally {
        output.close();
        writer.close();
      }
    } catch (IOException e) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e.toString());
    }
  }

  @Override
  public void inform(SolrCore core) {
    SolrResourceLoader loader = core.getResourceLoader();
    File schemaFile = new File(core.getSchemaResource());
    if(schemaFile.getParentFile() != null) {
      configurationDirectory = new File(loader.getConfigDir() + schemaFile.getParentFile());
    }
    else {
      configurationDirectory = new File(loader.getConfigDir());
    }
  }


  // SolrInfoMBean


  @Override
  public String getVersion() {
    return version + "-$Revision: 75166 $";
  }

  @Override
  public String getDescription() {
    return "A request handler to update synonyms, protected words, stop words, and content elevation configuration from within TYPO3.";
  }

  @Override
  public String getSource() {
    return null;
  }


}