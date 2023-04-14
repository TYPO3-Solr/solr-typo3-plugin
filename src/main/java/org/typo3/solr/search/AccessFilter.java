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

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;
import org.apache.solr.schema.IndexSchema;
import org.typo3.access.Rootline;
import org.typo3.access.RootlineElement;
import org.typo3.access.RootlineElementType;
import org.typo3.common.lang.StringUtils;
import org.apache.solr.search.ExtendedQueryBase;
import org.apache.solr.search.PostFilter;
import org.apache.solr.search.DelegatingCollector;
import org.apache.lucene.search.IndexSearcher;

/**
 * A filter to make sure a user can only see documents indexed by TYPO3 he's
 * allowed to see.
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public class AccessFilter extends ExtendedQueryBase implements PostFilter {

  /**
   * HashSet representation of userGroupList.
   */
  private HashSet<Integer> userGroupSet;

  /**
   * Field holding the groups required to access a document.
   */
  private String accessField;


  // constructors


  /**
   * Constructor which sets the field to be used to "access" and allows only
   * public documents to be returned.
   */
  public AccessFilter() {
    this("0");
  }

  @Override
  public boolean getCache() {
    return false;  // never cache
  }

  @Override
  public int getCost() {
    return Math.max(super.getCost(), 100); // Cost must be atleast 100, to ensure we run as a post-filter.
  }

  /**
   * Constructor which sets the field to be used to "access".
   *
   * @param userGroupList Comma separated list of groups the user has access to.
   */
  public AccessFilter(final String userGroupList) {
    this("access", userGroupList);
  }

  /**
   * Constructor which allows to set the field to be used and the groups a user
   * is member of for access checks.
   *
   * @param field The field to be used for access checks.
   * @param userGroupList Comma separated list of groups the user has access to.
   */
  public AccessFilter(final String field, final String userGroupList) {
    this.accessField = field;

    setUserGroupList(userGroupList);
  }

  /**
   * Recurse through the query tree, visiting any child queries
   * @param visitor a QueryVisitor to be called by each query in the tree
   */
  @Override
  public void visit(QueryVisitor visitor) {
    visitor.visitLeaf(this);
  }

  /**
   * This method iterates over the documents and marks documents as accessable that are granted
   * and have the access information in a single value field.
   *
   * @param doc
   * @param values
   * @throws IOException
   * @return boolean TRUE if access is granted, otherwise FALSE
     */

  private boolean handleSingleValueAccessField(int doc, SortedDocValues values) throws IOException {

    values.advance(doc);
    BytesRef bytes = values.lookupOrd(values.ordValue());
    String documentGroupList = bytes.utf8ToString();

    if (accessGranted(documentGroupList)) {
      return true;
    }
    return false;
  }


  /**
   * This method iterates over the documents and marks documents as accessable that are granted
   * and have the access information in a single value field.
   *
   * @param doc
   * @param multiValueSet
   * @throws IOException
   * @return boolean TRUE if access is granted, otherwise FALSE
     */
  private boolean handleMultivalueAccessField(int doc, SortedSetDocValues multiValueSet) throws IOException {
    long ord;
    multiValueSet.advance(doc);

    while ((ord = multiValueSet.nextOrd()) != SortedSetDocValues.NO_MORE_ORDS) {
      BytesRef bytes = multiValueSet.lookupOrd(ord);
      String documentGroupList = bytes.utf8ToString();

      if (accessGranted(documentGroupList)) {
	    return true;
      }
    }
    return false;
  }

  /**
   * A modified delegating collector that runs after queries and filters
   * but before sorting and grouping collectors, see the SOLR PostFilter interface.
   *
   * @param searcher
   * @return A delegating collector that can be called by SOLR for PostFilter processing.
   */

  @Override
  public DelegatingCollector getFilterCollector(IndexSearcher searcher) {
    return new DelegatingCollector() {
      SortedDocValues acls;
      SortedSetDocValues aclsSet;
      boolean isMultivalue;

      /**
       * This method is run whenever we reach the a new index segment, when
       * this happens we need to load a new set of DocValues.
       *
       * @param context
       * @throws IOException
       */

      public void doSetNextReader(LeafReaderContext context) throws IOException {
        DocValuesType type = context.reader().getFieldInfos().fieldInfo(accessField).getDocValuesType();
        isMultivalue = type.equals(DocValuesType.SORTED_SET);

        if (isMultivalue) {
          aclsSet = context.reader().getSortedSetDocValues(accessField);
        } else {
          acls = context.reader().getSortedDocValues(accessField);
        }
        super.doSetNextReader(context);
      }

      /**
       * Called for each document that need to be considered, if we do
       * not call super.collect, the document will effectively be filtered here.
       *
       *
       * @param doc
       * @throws IOException
       */

      @Override
      public void collect(int doc) throws IOException {
        if (isMultivalue && handleMultivalueAccessField(doc, aclsSet)) {
          super.collect(doc);
        }

        if (!isMultivalue && handleSingleValueAccessField(doc, acls)) {
          super.collect(doc);
        }
      }
    };
  }

  /**
   * Checks whether access is allowed based on the document's required groups
   * and the groups the user has access to.
   *
   * @param documentGroupList Comma separated list of groups required to access
   *                          a document
   * @return boolean TRUE if access is granted, FALSE otherwise
   */
  private boolean accessGranted(final String documentGroupList) {
    Boolean accessGranted = false;
    Rootline accessRootline = new Rootline(documentGroupList);

    for (RootlineElement element : accessRootline) {
      if (element.getType() == RootlineElementType.PAGE) {
        // for a page the user must have access to
        // at least one access group set for the page
        accessGranted = accessGrantedForPage(element.getAccessGroups());
      } else if (element.getType() == RootlineElementType.RECORD) {
        accessGranted = accessGrantedForRecord(element.getAccessGroups());
      } else {
        // for content the user must have access to
        // all access groups set for the content
        accessGranted = userGroupSet.containsAll(element.getAccessGroups());
      }

      if (!accessGranted) {
        break;
      }
    }

    return accessGranted;
  }

  /**
   * Checks whether the user has access to a certain page.
   *
   * To access a page the user must be member of at least one access group that
   * have been set for a page.
   *
   * @param pageElementAccess Access groups set for a page.
   * @return Boolean True if the user has access to the page, false otherwise.
   */
  private Boolean accessGrantedForPage(final HashSet<Integer> pageElementAccess) {
    Boolean accessGranted = false;

    for (Integer userGroup : userGroupSet) {
      if (pageElementAccess.contains(userGroup)) {
        accessGranted = true;
        break;
      }
    }

    return accessGranted;
  }

  /**
   * Checks whether the user has access to a certain record.
   *
   * To access a record the user must be member of at least one access group
   * that have been set for a record.
   *
   * @param recordElementAccess Access groups set for a record.
   * @return Boolean True if the user has access to the record, false otherwise.
   */
  private Boolean accessGrantedForRecord(final HashSet<Integer> recordElementAccess) {
    Boolean accessGranted = false;

    for (Integer userGroup : userGroupSet) {
      if (recordElementAccess.contains(userGroup)) {
        accessGranted = true;
        break;
      }
    }

    return accessGranted;
  }


  // utility


  /**
   * Sets the user group list and recalculates the set accordingly.
   * Makes sure that the set is always the same as the list.
   *
   * @param userGroupList comma separated list of user group Ids
   */
  private void setUserGroupList(final String userGroupList) {
    this.userGroupSet = StringUtils.commaSeparatedListToIntegerHashSet(userGroupList);
  }

  @Override
  public String toString(String field) {
    return getClass().getName() + " - " + this.accessField + ": " + StringUtils.implode(this.userGroupSet, ",");
  }

  @Override
  public int hashCode() {
    int result = 0;
    result = 31 * result + (userGroupSet != null ? userGroupSet.hashCode() : 0);
    result = 31 * result + (accessField != null ? accessField.hashCode() : 0);
    return result;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AccessFilter that = (AccessFilter) o;

    if (!this.userGroupSet.equals(that.userGroupSet)) {
      return false;
    }
    if (!this.accessField.equals(that.accessField)) {
      return false;
    }

    return true;
  }

}
