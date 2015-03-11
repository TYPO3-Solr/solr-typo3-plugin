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

import junit.framework.TestCase;

public class RootlineTest extends TestCase {


  public void testRootlineParsesAnAccessRootlineStringWithTheCorrectNumberOfElements() {
    String testAccessRootlineString = "1:1,2,3/2:4,5/3:5/c:1,2";

    Rootline testAccessRootline = new Rootline(testAccessRootlineString);
    assertTrue(testAccessRootline.size() == 4);
  }

  public void testRootlineIgnoresFaultyRootlineElements() {
    String testAccessRootlineString = "1:1,2,3/2:4,5:wrongFormat/3:5/c:1,2";

    Rootline testAccessRootline = new Rootline(testAccessRootlineString);
    assertTrue(testAccessRootline.size() == 3);
  }

  public void testToStringBuildsTheCorrectStringRepresentation() {
    String testAccessRootlineString = "1:1,2,3/2:4,5/3:5/c:1,2";

    Rootline testAccessRootline = new Rootline(testAccessRootlineString);
    assertEquals(testAccessRootlineString, testAccessRootline.toString());
  }
}
