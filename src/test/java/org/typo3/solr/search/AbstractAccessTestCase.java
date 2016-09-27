package org.typo3.solr.search;

import junit.framework.TestCase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

abstract public class AbstractAccessTestCase extends TestCase {
    protected Query getBooleanQueryFromSearchQueryAndFilter(Query searchQuery, AccessFilter filter) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(searchQuery, BooleanClause.Occur.MUST);
        builder.add(filter, BooleanClause.Occur.FILTER);
        return builder.build();
    }
}
