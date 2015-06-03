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


import java.io.File;
import java.io.StringReader;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.solr.util.RestTestBase;
import org.apache.solr.util.RestTestHarness;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.noggit.JSONParser;
import org.noggit.ObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.solr.core.ConfigOverlay.getObjectByPath;

public class AccessFilterQParserPluginTest extends RestTestBase {

    public static final Logger log = LoggerFactory.getLogger(AccessFilterQParserPluginTest.class);

    private static final String collection = "collection1";
    private static final String confDir = collection + "/conf";

    @Before
    public void before() throws Exception {
        File tmpSolrHome = createTempDir().toFile();
        File tmpConfDir = new File(tmpSolrHome, confDir);
        FileUtils.copyDirectory(new File(TEST_HOME()), tmpSolrHome.getAbsoluteFile());

        createJettyAndHarness(
            tmpSolrHome.getAbsolutePath(),
            "solrconfig.xml",
            "schema.xml",
            "/solr",
            true,
            null
        );
    }

    @After
    public void after() throws Exception {
        if (jetty != null) {
            jetty.stop();
            jetty = null;
        }
        client = null;
        if (restTestHarness != null) {
            restTestHarness.close();
        }
        restTestHarness = null;
    }

    @Test
    public void testPluginListsVersion() throws Exception {
        RestTestHarness harness = restTestHarness;
        Map plugins = getResponseMap("/admin/plugins?wt=json", harness);
        Object objectInfo = getObjectByPath(plugins, false, Arrays.asList("plugins", "OTHER", "org.typo3.solr.search.AccessFilterQParserPlugin"));

        if (objectInfo instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, String> pluginInfo = (LinkedHashMap<String, String>) objectInfo;
            String version = pluginInfo.get("version");

            assertTrue(version.startsWith("1."));
        } else {
            fail();
        }
    }

    public static Map getResponseMap(String path, RestTestHarness restHarness) throws Exception {
        String response = restHarness.query(path);
        try {
            return (Map) ObjectBuilder.getVal(new JSONParser(new StringReader(response)));
        } catch (JSONParser.ParseException e) {
            log.error(response);
            return Collections.emptyMap();
        }
    }
}
