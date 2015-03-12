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

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;


public class AccessFilterTest extends TestCase {

	private Directory dir;
	private IndexReader reader;
	private IndexSearcher searcher;
	private Query allDocs;


	protected void setUp() throws Exception {
		dir = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();

		Document[] docs = {
			getDocument("public1", "0"),
			getDocument("public2", "0"),

			getDocument("protected1_group1", "1"),
			getDocument("protected2_group1,2", "1,2")
		};

		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, conf);

		for (Document doc : docs) {
			writer.addDocument(doc);
		}
		writer.close();

		reader = DirectoryReader.open(dir);
		searcher = new IndexSearcher(reader);
		allDocs = new MatchAllDocsQuery();
	}

	protected Document getDocument(String title, String access) {
		Document doc = new Document();

		doc.add(new StringField("title", title, Field.Store.YES));
		doc.add(new SortedDocValuesField("access", new BytesRef(access)));

		return doc;
	}

	protected void tearDown() throws Exception {
		reader.close();
		dir.close();
	}

	public void testAccessFilterFindsOnlyPublicDocumentsWhenNotLoggedIn() throws Exception {
		Filter filter = new AccessFilter();

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("returns only public documents", 2, hits.totalHits);
	}

	public void testAccessFilterFindsDocumentsAllowedForGroup1() throws Exception {
		Filter filter = new AccessFilter("0,1");

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("returns public documents and for group 1", 3, hits.totalHits);
		assertEquals("allows access for document 'protected1_group1'",
			"protected1_group1",
			searcher.doc(hits.scoreDocs[2].doc).get("title")
		);
	}

	public void testAccessFilterFindsDocumentsRequiringAccessForGroup1And2() throws Exception {
		Filter filter = new AccessFilter("0,1,2");

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("returns public documents and for group 1 and group 2", 4, hits.totalHits);
	}

	public void testAccessFilterFiltersDocumentsWithInsufficientAccess() throws Exception {
		Filter filter = new AccessFilter("0,2");

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("returns accessible documents", 2, hits.totalHits);
	}

}
