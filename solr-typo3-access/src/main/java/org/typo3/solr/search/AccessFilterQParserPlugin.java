/**
 * Copyright 2010-2011 Ingo Renner <ingo@typo3.org>
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

package org.typo3.solr.search;

import java.net.URL;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;


/**
 * A query parser plugin to add a TYPO3 access filter to search queries.
 *
 * @author Ingo Renner <ingo@typo3.org> *
 */
public class AccessFilterQParserPlugin extends QParserPlugin implements SolrInfoMBean {

  /**
   * The name of the query parser. Used in solrconfig.xml and in queries.
   */
  public static String NAME = "typo3access";

  /**
   * Query Parser Plugin Version.
   */
  public static final String version = "1.2.0";

  /**
   * Implementation of NamedListInitializedPlugin.init().
   *
   * @param args Arguments
   * @see org.apache.solr.util.plugin.NamedListInitializedPlugin#init(org.apache.solr.common.util.NamedList)
   */
  public void init(NamedList args) {
  }

  /**
   * Implementation of QParserPlugin.createParser().
   *
   * @see org.apache.solr.search.QParserPlugin#createParser(java.lang.String,
   * org.apache.solr.common.params.SolrParams,
   * org.apache.solr.common.params.SolrParams,
   * org.apache.solr.request.SolrQueryRequest)
   */
  @Override
  public QParser createParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
      // register plugin so that we can detect it in http://host:port/path-to-solr/admin/plugins
    req.getCore().getInfoRegistry().put(getName(), this);

    return new AccessFilterQParser(qstr, localParams, params, req);
  }


  // SolrInfoMBean


  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public String getVersion() {
    return version + "-$Revision: 88365 $";
  }

  @Override
  public String getDescription() {
    return "A filter plugin to support TYPO3 access restrictions.";
  }

  @Override
  public Category getCategory() {
    return SolrInfoMBean.Category.OTHER;
  }

  @Override
  public String getSource() {
    return null;
  }

  @Override
  public URL[] getDocs() {
    return null;
  }

  @Override
  public NamedList getStatistics() {
    return new SimpleOrderedMap();
  }

}
