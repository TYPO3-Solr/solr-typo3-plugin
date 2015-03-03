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

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixFilter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


public class AccessFilterWithAccessRootlineTest extends TestCase {

	private Directory dir;
	private IndexReader reader;
	private IndexSearcher searcher;
	private Query allDocs;
	private FilteredQuery allRecordDocs;


	protected void setUp() throws Exception {
		dir = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_32);

		Document[] docs = {
			getDocument("public1", "0"),
			getDocument("public2", "0"),
			getDocument("publicRecord3", "r:0"),

			getDocument("protected1__1_2||3_1", "1:1/2:2,3/c:1"),
			getDocument("protected2__1_2||3_0", "1:1/2:2,3/c:0"),
			getDocument("protected3__1_2||3_1&&2", "1:1/2:2,3/c:1,2"),
			getDocument("protected4__1_2||3_1&&2&&3", "1:1/2:2,3/c:1,2,3"),

			getDocument("protectedRecord5__r1||2||3", "r:1,2,3")
		};

		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_4_10_3, analyzer);
		conf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

		IndexWriter writer = new IndexWriter(dir, conf);

		for (Document doc : docs) {
			writer.addDocument(doc);
		}
		writer.close();

		reader = DirectoryReader.open(dir);
		searcher = new IndexSearcher(reader);
		allDocs = new MatchAllDocsQuery();

    PrefixFilter recordFilter = new PrefixFilter(new Term("access", "r"));
    allRecordDocs = new FilteredQuery(allDocs, recordFilter);
	}

	protected Document getDocument(String title, String access) {
		Document doc = new Document();

		doc.add(new Field("title", title, Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("access", access, Field.Store.YES, Field.Index.NOT_ANALYZED));

		return doc;
	}

	protected void tearDown() throws Exception {
		reader.close();
		dir.close();
	}

	public void testAccessFilterFindsOnlyPublicDocumentsWhenNotLoggedIn() throws Exception {
		Filter filter = new AccessFilter();

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("only public documents", 3, hits.totalHits);
	}

	public void testAccessFilterFindsDocumentsAllowedForGroup1() throws Exception {
		Filter filter = new AccessFilter("0,1");

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("public documents and for group 1", 4, hits.totalHits);
	}

	public void testAccessFilterFindsDocumentsAllowedForGroup1And2() throws Exception {
    Filter filter = new AccessFilter("0,1,2");

    TopDocs hits = searcher.search(allDocs, filter, 10);
    assertEquals("public documents and for groups 1 and 2", 7, hits.totalHits);

    assertEquals("allows access for document 'protected1__1_2||3_1'",
      "protected1__1_2||3_1",
      searcher.doc(hits.scoreDocs[3].doc).get("title")
    );
    assertEquals("allows access for document 'protected2__1_2||3_0'",
      "protected2__1_2||3_0",
      searcher.doc(hits.scoreDocs[4].doc).get("title")
    );
    assertEquals("allows access for document 'protected3__1_2||3_1&&2'",
      "protected3__1_2||3_1&&2",
      searcher.doc(hits.scoreDocs[5].doc).get("title")
    );
  }

	public void testAccessFilterFindsDocumentsAllowedForGroup1And3() throws Exception {
    Filter filter = new AccessFilter("0,1,3");

    TopDocs hits = searcher.search(allDocs, filter, 10);
    assertEquals("public documents and for groups 1 and 3", 6, hits.totalHits);

    assertEquals("allows access for document 'protected1__1_2||3_1'",
      "protected1__1_2||3_1",
      searcher.doc(hits.scoreDocs[3].doc).get("title")
    );
    assertEquals("allows access for document 'protected2__1_2||3_0'",
      "protected2__1_2||3_0",
      searcher.doc(hits.scoreDocs[4].doc).get("title")
    );
  }

	public void testAccessFilterFindsDocumentsRequiringAccessForGroup1And2And3() throws Exception {
    Filter filter = new AccessFilter("0,1,2,3");

    TopDocs hits = searcher.search(allDocs, filter, 10);
    assertEquals("public documents and for groups 1,2,3", 8, hits.totalHits);

    assertEquals("allows access for document 'protected1__1_2||3_1'",
      "protected1__1_2||3_1",
      searcher.doc(hits.scoreDocs[3].doc).get("title")
    );
    assertEquals("allows access for document 'protected2__1_2||3_0'",
      "protected2__1_2||3_0",
      searcher.doc(hits.scoreDocs[4].doc).get("title")
    );
    assertEquals("allows access for document 'protected3__1_2||3_1&&2'",
      "protected3__1_2||3_1&&2",
      searcher.doc(hits.scoreDocs[5].doc).get("title")
    );
    assertEquals("allows access for document 'protected4__1_2||3_1&&2&&3'",
      "protected4__1_2||3_1&&2&&3",
      searcher.doc(hits.scoreDocs[6].doc).get("title")
    );
    assertEquals("allows access for record document 'protectedRecord5__r1||2||3'",
      "protectedRecord5__r1||2||3",
      searcher.doc(hits.scoreDocs[7].doc).get("title")
    );
  }

	public void testAccessFilterFiltersDocumentsWithInsufficientAccess() throws Exception {
		Filter filter = new AccessFilter("0,2");

		TopDocs hits = searcher.search(allDocs, filter, 10);
		assertEquals("public documents and documents for group 2 only", 4, hits.totalHits);
	}

  public void testAccessFilterFindsRecordDocumentsRequiringAccessForAtLeastGroup1() throws Exception {
    Filter accessFilter = new AccessFilter("0,1");

    TopDocs hits = searcher.search(allRecordDocs, accessFilter, 10);
    assertEquals("public documents and documents accessible to group 1", 2, hits.totalHits);
    assertEquals("allows access for public record document 'publicRecord3'",
      "publicRecord3",
      searcher.doc(hits.scoreDocs[0].doc).get("title")
    );
    assertEquals("allows access for protected record document 'protectedRecord5__r1||2||3'",
      "protectedRecord5__r1||2||3",
      searcher.doc(hits.scoreDocs[1].doc).get("title")
    );
	}

  public void testAccessFilterFindsOnlyPublicRecordDocumentsWhenNotLoggedIn() throws Exception {
   Filter accessFilter = new AccessFilter();

    TopDocs hits = searcher.search(allRecordDocs, accessFilter, 10);
    assertEquals("public documents only", 1, hits.totalHits);
  }

}
