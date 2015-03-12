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

package org.typo3.access;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.typo3.common.lang.StringUtils;

/**
 * "Access Rootline", represents all pages and specifically those setting
 * frontend user group access restrictions in a page's rootline.
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public class Rootline extends ArrayList<RootlineElement> {

  /**
   *
   */
  private static final long serialVersionUID = 5121163338622263751L;

  /**
   * Delimiter for page and content access right elements in the rootline.
   */
  private String elementDelimiter = "/";


  /**
   * Constructor, turns a string representation of an access rootline into an
   * object representation.
   *
   * The access rootline only contains pages which set frontend user access
   * restrictions and extend them to sub-pages. The format is as follows:
   *
   * pageId1:group1,group2/pageId2:group3/c:group1,group4,groupN
   *
   * The single elements of the access rootline are separated by a slash
   * character. All but the last elements represent pages, the last element
   * defines the access restrictions applied to the page's content elements and
   * records shown on the page.
   * Each page element is composed by the page ID of the page setting frontend
   * user access restrictions, a colon, and a comma separated list of frontend
   * user group IDs restricting access to the page.
   * The content access element does not have a page ID, instead it replaces the
   * ID by a lower case C.
   *
   * The groups for page elements are compared using OR, so the user needs to be
   * a member of only one of the groups listed for a page. The elements are
   * checked combined using AND, so the user must be member of at least one
   * group in each page element. However, the groups in the content access
   * element are checked using AND. So the user must be member of all the groups
   * listed in the content access element to see the document.
   *
   * An access rootline for a generic record could instead be short like this:
   *
   * r:group1,group2,groupN
   *
   * In this case the lower case R tells us that we're dealing with a record
   * like tt_news or the like. For records the groups are checked using OR
   * instead of using AND as it would be the case with content elements.
   *
   * @param accessRootline Access Rootline String representation.
   */
  public Rootline(String accessRootline) {
    StringTokenizer st = new StringTokenizer(accessRootline, elementDelimiter);

    while (st.hasMoreTokens()) {
      String elementAccess = st.nextToken();

      try {
        add(new RootlineElement(elementAccess));
      } catch (RootlineElementFormatException e) {
        // just ignore the faulty element for now, might log this later
      }
    }
  }

  /**
   * Returns the String representation of the access rootline.
   *
   * @return String String representation of the access rootline.
   */
  public String toString() {
    return StringUtils.implode(this, elementDelimiter);
  }

}
