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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;
import org.typo3.access.Rootline;
import org.typo3.access.RootlineElement;
import org.typo3.access.RootlineElementType;
import org.typo3.common.lang.StringUtils;


/**
 * A filter to make sure a user can only see documents indexed by TYPO3 he's
 * allowed to see.
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public class AccessFilter extends Filter {

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

  /**
   * Constructor which sets the field to be used to "access".
   *
   * @param userGroupList Comma separated list of groups the user has access to.
   */
  public AccessFilter(String userGroupList) {
    this("access", userGroupList);
  }

  /**
   * Constructor which allows to set the field to be used and the groups a user
   * is member of for access checks.
   *
   * @param field The field to be used for access checks.
   * @param userGroupList Comma separated list of groups the user has access to.
   */
  public AccessFilter(String field, String userGroupList) {
    this.accessField = field;
    setUserGroupList(userGroupList);
  }


  // filter


  /**
   * Filters the documents based on the access granted.
   *
   * @param reader The index reader
   * @return OpenBitSet
   * @throws IOException When an error occurs while reading from the index.
   */
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    String[] access = FieldCache.DEFAULT.getStrings(reader, accessField);
    OpenBitSet bits = new OpenBitSet(reader.maxDoc());

    TermDocs td = reader.termDocs(null);

    try {

      while (td.next()) {
        int docId = td.doc();

        if (accessGranted(access[docId])) {
          bits.set(docId);
        }
      }

    } finally {
      td.close();
    }

    return bits;
  }

  /**
   * Checks whether access is allowed based on the document's required groups
   * and the groups the user has access to.
   *
   * @param documentGroupList Comma separated list of groups required to access a document
   * @return boolean TRUE if access is granted, FALSE otherwise
   */
  private boolean accessGranted(String documentGroupList) {
    Boolean accessGranted = false;
    Rootline accessRootline = new Rootline(documentGroupList);

    for (RootlineElement element : accessRootline) {
      if (element.getType() == RootlineElementType.PAGE) {
        // for a page the user must have access to at least one access group set for the page
        accessGranted = accessGrantedForPage(element.getAccessGroups());
      } else if (element.getType() == RootlineElementType.RECORD) {
        accessGranted = accessGrantedForRecord(element.getAccessGroups());
      } else {
        // for content the user must have access to all access groups set for the content
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
  private Boolean accessGrantedForPage(HashSet<Integer> pageElementAccess) {
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
  private Boolean accessGrantedForRecord(HashSet<Integer> recordElementAccess) {
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
  private void setUserGroupList(String userGroupList) {
    this.userGroupSet = StringUtils.commaSeparatedListToIntegerHashSet(userGroupList);
  }

}
