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

import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.common.params.CommonParams;
import org.junit.BeforeClass;


/**
 * Test class to test the Query Parser Plugin
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public class AccessFilterQParserTest extends SolrTestCaseJ4 {

	// setUp and tearDown

  @BeforeClass
  public static void beforeClass() throws Exception {
    initCore("solrconfig.xml", "schema.xml");
    createIndex();
  }

  private static void createIndex() {
    assertU(addDocument("public1", "0"));
    assertU(addDocument("public2", "0"));
    assertU(addDocument("protected1_group1", "1"));
    assertU(addDocument("protected2_group1,2", "1,2"));

    assertU(commit());
  }


	// tests


	public void testSolrFindsOnlyPublicDocumentsWhenNotLoggedIn() {
		assertQ(
			"couldn't find public documents",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0",
					CommonParams.VERSION, "2.2"
				),
			"//result[@numFound=2]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']"
		);
	}

	public void testSolrFindsDocumentsAllowedForGroup1() throws Exception {
		assertQ(
			"couldn't find public documents and documents for group 1",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,1",
					CommonParams.VERSION, "2.2"
				),
			"//result[@numFound=3]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='protected1_group1']"
		);
	}

	public void testSolrFindsDocumentsRequiringAccessForGroup1And2() throws Exception {
		assertQ(
			"couldn't find public documents and documents for group 1 and group 2",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,1,2",
					CommonParams.VERSION, "2.2"
				),
			"//result[@numFound=4]"
		);
	}

	public void testSolrFiltersDocumentsWithInsufficientAccess() throws Exception {
		assertQ(
			"found public documents only",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,2",
					CommonParams.VERSION, "2.2"
				),
			"//result[@numFound=2]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']"
		);
	}


	// utilities


	private static String addDocument(String title, String access) {
		return adoc(
			"id", title,
			"appKey", "AccessFilterQParserTest",
			"type", "JUnitTestDocument",
			"title", title,
			"access", access
		);
	}

}
