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

import org.apache.solr.SolrTestCaseJ4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test class to test the Query Parser Plugin
 *
 * @author Ingo Renner <ingo@typo3.org>
 */
public class AccessFilterQParserWithRootLineTest extends SolrTestCaseJ4 {

	// setUp and tearDown

	@Before
	public void before() throws Exception {
		System.setProperty("solr.directoryFactory", "solr.MockDirectoryFactory");
		initCore("solrconfig.xml", "schema.xml");
		createIndex();
	}


	@After
	public void after() throws Exception {
		deleteCore();
	}

	private static void createIndex() {
		assertU(addDocument("public1", "0"));
		assertU(addDocument("public2", "0"));
		assertU(addDocument("publicRecord3", "r:0", "record"));
		assertU(addDocument("protected1__1_2||3_1", "1:1/2:2,3/c:1"));
		assertU(addDocument("protected2__1_2||3_0", "1:1/2:2,3/c:0"));
		assertU(addDocument("protected3__1_2||3_1&&2", "1:1/2:2,3/c:1,2"));
		assertU(addDocument("protected4__1_2||3_1&&2&&3", "1:1/2:2,3/c:1,2,3"));
		assertU(addDocument("protectedRecord5__r1||2||3", "r:1,2,3", "record"));


		assertU(commit());
	}


	// tests

	@Test
	public void testSolrFindsOnlyPublicDocumentsWhenNotLoggedIn() {
		assertQ(
			"couldn't find public documents",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0"
			),
			"//result[@numFound=3]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='publicRecord3']"
		);
	}

	@Test
	public void testSolrFindsDocumentsAllowedForGroup1() throws Exception {
		assertQ(
			"couldn't find public documents and documents for group 1",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,1"
				),
			"//result[@numFound=4]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='publicRecord3']",
			"//str[@name='id'][.='protectedRecord5__r1||2||3']"
		);
	}

	@Test
	public void testSolrFindsDocumentsRequiringAccessForGroup1And2() throws Exception {
		assertQ(
			"couldn't find public documents and documents for group 1 and group 2",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,1,2"
				),
			"//result[@numFound=7]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='publicRecord3']",
			"//str[@name='id'][.='protectedRecord5__r1||2||3']",
			"//str[@name='id'][.='protected1__1_2||3_1']",
			"//str[@name='id'][.='protected2__1_2||3_0']",
			"//str[@name='id'][.='protected3__1_2||3_1&&2']"
		);
	}
	@Test

	public void testSolrFindsDocumentsRequiringAccessForGroup1And3() throws Exception {
		assertQ(
			"couldn't find public documents and documents for group 1 and group 3",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,1,3"
				),
			"//result[@numFound=6]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='publicRecord3']",
			"//str[@name='id'][.='protectedRecord5__r1||2||3']",
			"//str[@name='id'][.='protected1__1_2||3_1']",
			"//str[@name='id'][.='protected2__1_2||3_0']"
		);
	}

	@Test
	public void testSolrFindsDocumentsRequiringAccessForGroup1And2And3() throws Exception {
		assertQ(
			"couldn't find public documents and documents for group 1,2 and 3",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,1,2,3"
				),
			"//result[@numFound=8]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='publicRecord3']",
			"//str[@name='id'][.='protectedRecord5__r1||2||3']",
			"//str[@name='id'][.='protected1__1_2||3_1']",
			"//str[@name='id'][.='protected2__1_2||3_0']",
			"//str[@name='id'][.='protected3__1_2||3_1&&2']",
			"//str[@name='id'][.='protected4__1_2||3_1&&2&&3']"
		);
	}

	@Test
	public void testSolrFiltersDocumentsWithInsufficientAccess() throws Exception {
		assertQ(
			"found public documents only",
			req(
					"q", "*:*",
					"qt", "/select",
					"fq", "{!typo3access}0,2"
				),
			"//result[@numFound=4]",
			"//str[@name='id'][.='public1']",
			"//str[@name='id'][.='public2']",
			"//str[@name='id'][.='publicRecord3']",
			"//str[@name='id'][.='protectedRecord5__r1||2||3']"
		);
	}

	@Test
	public void testSolrFindsOnlyPublicRecordDocumentsWhenNotLoggedIn() throws Exception {
		assertQ(
			"couldn't find public record documents",
			req(
					"q", "type:record",
					"qt", "/select",
					"fq", "{!typo3access}0"
				),
			"//result[@numFound=1]",
			"//str[@name='id'][.='publicRecord3']"
		);
	}

	@Test
	public void testSolrFindsRecordDocumentsRequiringAccessForAtleastGroup1() throws Exception {
		assertQ(
			"couldn't find public record documents",
			req(
					"q", "type:record",
					"qt", "/select",
					"fq", "{!typo3access}0,1"
				),
			"//result[@numFound=2]",
			"//str[@name='id'][.='publicRecord3']",
			"//str[@name='id'][.='protectedRecord5__r1||2||3']"
		);
	}


	// utilities


	private static String addDocument(String title, String access) {
		return adoc(
			"id", title,
			"title", title,
			"access", access
		);
	}

	private static String addDocument(String title, String access, String type) {
		return adoc(
			"id", title,
			"title", title,
			"type", type,
			"access", access
		);
	}



}
