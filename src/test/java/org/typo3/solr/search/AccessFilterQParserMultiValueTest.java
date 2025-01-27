/**
 * Copyright 2010-2015 Ingo Renner <ingo@typo3.org>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
 * This testcase makes sure, that the access filter plugin is working,
 * when it is used with a multivalue field.
 *
 * @author Timo Hund <timo.hund@dkd.de>
 * */
public class AccessFilterQParserMultiValueTest extends SolrTestCaseJ4 {

    // setUp and tearDown

    @Before
    public void before() throws Exception {
        System.setProperty("solr.directoryFactory", "solr.MockDirectoryFactory");
        initCore("solrconfig.xml", "schema.xml");
    }

    @After
    public void after() throws Exception {
        deleteCore();
    }

    // tests

    @Test
    public void testPrivateDocumentIsFoundBySecondAccessValue() {
        assertU(addDocument("private1", "1", "2", "3"));
        assertU(commit());

        assertQ(
                "couldn't find protected document",
                req(
                        "q", "*:*",
                        "qt", "/select",
                        // we need to pass the custom multivalue field as local param
                        "fq", "{!typo3access t3.access.field=accesses}2"
                ),
                "//result[@numFound=1]",
                "//str[@name='id'][.='private1']"
        );
    }

    @Test
    public void testOnlyFirstDocumentIsFoundWhenSecondDocumentIsNotAccessable() {
        assertU(addDocument("private1", "1", "2", "3"));
        assertU(addDocument("private2", "3", "4", "5"));

        assertU(commit());

        assertQ(
                "couldn't find protected document",
                req(
                        "q", "*:*",
                        "qt", "/select",
                        // we need to pass the custom multivalue field as local param
                        "fq", "{!typo3access t3.access.field=accesses}2"
                ),
                "//result[@numFound=1]",
                "//str[@name='id'][.='private1']"
        );
    }

    @Test
    public void testBothDocumentGetFoundWhenBothAreAccessable() {
        assertU(addDocument("private1", "1", "2", "3"));
        assertU(addDocument("private2", "3", "4", "5"));

        assertU(commit());

        // we expect that that access to both documents get granted because we filter on group 3
        // and both documents contain group 3
        assertQ(
                "couldn't find both protected documents",
                req(
                        "q", "*:*",
                        "qt", "/select",
                        // we need to pass the custom multivalue field as local param
                        "fq", "{!typo3access t3.access.field=accesses}3"
                ),
                "//result[@numFound=2]",
                "//str[@name='id'][.='private1']",
                "//str[@name='id'][.='private2']"
        );
    }

    @Test
    public void testNoDocumentIsFoundWhenOnlyPrivateDocumentsExist() {
        assertU(addDocument("private1", "1", "2", "3"));
        assertU(addDocument("private2", "3", "4", "5"));

        assertU(commit());

        // we expect that that access to both documents get granted because we filter on group 3
        // and both documents contain group 3
        assertQ(
                "couldn't find both protected documents",
                req(
                        "q", "*:*",
                        "qt", "/select",
                        // we need to pass the custom multivalue field as local param
                        "fq", "{!typo3access t3.access.field=accesses}0"
                ),
                "//result[@numFound=0]"
        );
    }
    // utilities


    private static String addDocument(String title, String access1, String access2, String access3) {
        return adoc(
                "id", title,
                "title", title,
                "accesses", access1,
                "accesses", access2,
                "accesses", access3
        );
    }

}
