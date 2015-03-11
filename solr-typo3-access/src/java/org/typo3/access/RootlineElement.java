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

package org.typo3.access;

import java.util.HashSet;

import org.typo3.common.lang.StringUtils;

/**
 * An element in the "Access Rootline". Represents the frontend user group
 * access restrictions for a page or a page's content.
 *
 * @author Ingo Renner <ingo@typo3.org>
 * @see Rootline
 */
public class RootlineElement {

  /**
   * Delimiter between the page ID and the groups set for a page.
   */
  private String pageIdGroupDelimiter = ":";

  /**
   * Access type, either page (default) or content. Depending on the type,
   * access is granted differently. For pages the user must meet at least one
   * group requirement, for content all group requirements must be met.
   */
  private RootlineElementType type = RootlineElementType.PAGE;

  /**
   * Page Id for the element. Null for the content type.
   */
  private Integer id;

  /**
   * Set of access groups assigned to the element.
   */
  private HashSet<Integer> accessGroups;


  /**
   * Constructor.
   *
   * @param element String representation of an element in the access rootline, usually of the form pageId:commaSeparatedPageAccessGroups
   * @throws RootlineElementFormatException on wrong access format.
   */
  public RootlineElement(String element) throws RootlineElementFormatException {
    String[] elementAccess = StringUtils.explode(element, pageIdGroupDelimiter);
    String elementGroups;

    if (elementAccess[0].equals("c") || elementAccess.length == 1) {
      // the content access groups part of the access rootline
      id = null;
      type = RootlineElementType.CONTENT;

      if (elementAccess.length == 1) {
        elementGroups = elementAccess[0];
      } else {
        elementGroups = elementAccess[1];
      }
    } else if (elementAccess[0].equals("r")) {
      // a record access rootline element
      id = null;
      type = RootlineElementType.RECORD;

      elementGroups = elementAccess[1];
    } else {
      // must be page element type
      if (elementAccess.length != 2) {
        throw new RootlineElementFormatException();
      }

      id = Integer.parseInt(elementAccess[0]);
      elementGroups = elementAccess[1];
    }

    accessGroups = StringUtils.commaSeparatedListToIntegerHashSet(elementGroups);
  }

  /**
   * Returns the String representation of an access rootline element.
   *
   * @return String
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (type == RootlineElementType.CONTENT) {
      sb.append("c");
    } else if (type == RootlineElementType.RECORD) {
      sb.append("r");
    } else {
      sb.append(id);
    }

    sb.append(pageIdGroupDelimiter);
    sb.append(StringUtils.implode(accessGroups, ","));

    return new String(sb);
  }

  /**
   * Gets the page Id for page type elements.
   *
   * @return Integer Page Id.
   */
  public Integer getId() {
    return id;
  }

  /**
   * Gets the element's type.
   *
   * @return {@link RootlineElementType}
   */
  public RootlineElementType getType() {
    return type;
  }

  /**
   * Gets the element's access group restrictions.
   *
   * @return HashSet<Integer>
   */
  public HashSet<Integer> getAccessGroups() {
    return accessGroups;
  }

}
