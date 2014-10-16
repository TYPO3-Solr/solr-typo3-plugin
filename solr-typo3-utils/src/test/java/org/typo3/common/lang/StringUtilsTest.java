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

package org.typo3.common.lang;

import java.util.ArrayList;
import java.util.HashSet;
import junit.framework.TestCase;


public class StringUtilsTest extends TestCase {

  public void testImplodeCorrectlyImplodesCollections() {
    ArrayList<Integer> testCollection = new ArrayList<Integer>();
    testCollection.add(1);
    testCollection.add(2);
    testCollection.add(3);

    String testString = "1,2,3";

    assertEquals(testString, StringUtils.implode(testCollection, ","));
  }

  public void testExplodeCorrectlyExplodesStringsToStringArrays() {
    String testString = "one,two,three";
    String[] expectedResult = {"one", "two", "three"};

    String[] actualResult = StringUtils.explode(testString, ",");

    for (int i = 0; i < actualResult.length; i++) {
      assertTrue(actualResult[i].equals(expectedResult[i]));
    }

  }

  public void testCommaSeparatedListToIntegerHashSetCreatesCorrectHashSets() {
    String testString = "1,2,3";

    HashSet<Integer> actualResult = StringUtils.commaSeparatedListToIntegerHashSet(testString);

    assertTrue(actualResult.size() == 3);
    assertTrue(actualResult.contains(new Integer(1)));
    assertTrue(actualResult.contains(new Integer(2)));
    assertTrue(actualResult.contains(new Integer(3)));
  }

}
