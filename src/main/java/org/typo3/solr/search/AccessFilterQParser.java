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

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;
import org.typo3.solr.common.params.AccessParams;


/**
 * A QParser to add the TYPO3 access filter to the current query.
 *
 * @author Ingo Renner <ingo@typo3.org> *
 */
public class AccessFilterQParser extends QParser {

  /**
   * The access filter to be applied to the query.
   */
  private final AccessFilter accessFilter;

  /**
   * Constructor.
   *
   * @param qstr Search term
   * @param localParams Local parameters
   * @param params GET parameters
   * @param req Solr request
   */
  public AccessFilterQParser(final String qstr, final SolrParams localParams, final SolrParams params, final SolrQueryRequest req) {
    super(qstr, localParams, params, req);

    this.accessFilter = new AccessFilter(
      localParams.get(AccessParams.ACCESS_FIELD, "access"),
      qstr
    );
  }

  @Override
  public final Query parse() throws SyntaxError {
    return accessFilter;
  }
}
