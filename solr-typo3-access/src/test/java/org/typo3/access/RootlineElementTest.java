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

import junit.framework.TestCase;

public class RootlineElementTest extends TestCase {

  public void testRootlineElementThrowsExceptionForTooManyColons() {
    try {
      RootlineElement wrongFormat = new RootlineElement("1:1,2,3:wrongFormat");

      fail("Should have raised a RootlineElementFormatException");
    } catch (RootlineElementFormatException e) {
    }
  }

  public void testRootlineElementCorrectlyParsesThePageId() {
    try {
      RootlineElement accessRootlinePageElement = new RootlineElement("1:1,2,3");
      assertTrue(accessRootlinePageElement.getId().equals(1));

      RootlineElement accessRootlineContentElement = new RootlineElement("c:1,2,3");
      assertNull(accessRootlineContentElement.getId());

      RootlineElement accessRootlineRecordElement = new RootlineElement("r:1,2,3");
      assertNull(accessRootlineRecordElement.getId());
    } catch (RootlineElementFormatException e) {
    }
  }

  public void testRootlineElementCorrectlyDeterminesTheElementType() {
    try {
      RootlineElement accessRootlinePageElement = new RootlineElement("1:1,2,3");
      assertTrue(accessRootlinePageElement.getType() == RootlineElementType.PAGE);

      RootlineElement accessRootlineContentElement = new RootlineElement("c:1,2,3");
      assertTrue(accessRootlineContentElement.getType() == RootlineElementType.CONTENT);

      RootlineElement accessRootlineRecordElement = new RootlineElement("r:1,2,3");
      assertTrue(accessRootlineRecordElement.getType() == RootlineElementType.RECORD);
    } catch (RootlineElementFormatException e) {
    }
  }

  public void testRootlineElementCorrectlyParsesTheUserGroups() {
    try {
      HashSet<Integer> expectedResult = new HashSet<Integer>();
      expectedResult.add(1);
      expectedResult.add(2);
      expectedResult.add(3);

      RootlineElement accessRootlineElement = new RootlineElement("1:1,2,3");
      HashSet<Integer> actualResult = accessRootlineElement.getAccessGroups();

      assertTrue(actualResult.equals(expectedResult));
    } catch (RootlineElementFormatException e) {
    }
  }

  public void testToStringBuildsTheCorrectStringRepresentation() {
    String accessRootlinePageElementString = "1:1,2,3";
    String accessRootlineContentElementString = "c:1,2,3";
    String accessRootlineRecordElementString = "r:1,2,3";
    String accessRootlineBackwardsCompatibleContentElementString = "1,2,3";

    try {
      RootlineElement accessRootlinePageElement = new RootlineElement(accessRootlinePageElementString);
      assertEquals(accessRootlinePageElementString, accessRootlinePageElement.toString());

      RootlineElement accessRootlineContentElement = new RootlineElement(accessRootlineContentElementString);
      assertEquals(accessRootlineContentElementString, accessRootlineContentElement.toString());

      RootlineElement accessRootlineRecordElement = new RootlineElement(accessRootlineRecordElementString);
      assertEquals(accessRootlineRecordElementString, accessRootlineRecordElement.toString());

      RootlineElement accessRootlineBacwardsCompatibleContentElement = new RootlineElement(accessRootlineBackwardsCompatibleContentElementString);
        // should create the new format out of the old format
      assertEquals(accessRootlineContentElementString, accessRootlineBacwardsCompatibleContentElement.toString());
    } catch (RootlineElementFormatException e) {
    }
  }

}
