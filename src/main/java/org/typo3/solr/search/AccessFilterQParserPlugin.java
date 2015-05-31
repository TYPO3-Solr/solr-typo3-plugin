/**
 * Copyright 2010-2015 Ingo Renner <ingo@typo3.org>
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
  public static final String NAME = "typo3access";

  /**
   * Query Parser Plugin Version.
   */
  private static final String version = "1.5.0";

  /**
   * Implementation of NamedListInitializedPlugin.init().
   *
   * @param args Arguments
   * @see org.apache.solr.util.plugin.NamedListInitializedPlugin#init(org.apache.solr.common.util.NamedList)
   */
  public void init(final NamedList args) {
  }

  /**
   * Implementation of QParserPlugin.createParser().
   *
   * @see org.apache.solr.search.QParserPlugin#createParser(java.lang.String,
   * org.apache.solr.common.params.SolrParams,
   * org.apache.solr.common.params.SolrParams,
   * org.apache.solr.request.SolrQueryRequest)
   *
   * @param qstr Search term
   * @param localParams Local parameters
   * @param params GET parameters
   * @param req Solr request
   * @return QParser
   */
  @Override
  public final QParser createParser(final String qstr, final SolrParams localParams, final SolrParams params, final SolrQueryRequest req) {
      // register plugin so that we can detect it in http://host:port/path-to-solr/admin/plugins
    req.getCore().getInfoRegistry().put(getName(), this);

    return new AccessFilterQParser(qstr, localParams, params, req);
  }


  // SolrInfoMBean


  @Override
  public final String getName() {
    return this.getClass().getName();
  }

  @Override
  public final String getVersion() {
    return version;
  }

  @Override
  public final String getDescription() {
    return "A filter plugin to support TYPO3 access restrictions.";
  }

  @Override
  public final Category getCategory() {
    return SolrInfoMBean.Category.OTHER;
  }

  @Override
  public final String getSource() {
    return null;
  }

  @Override
  public final URL[] getDocs() {
    return null;
  }

  @Override
  public final NamedList getStatistics() {
    return new SimpleOrderedMap();
  }

}
