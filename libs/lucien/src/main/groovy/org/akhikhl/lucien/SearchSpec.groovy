package org.akhikhl.lucien
import org.apache.lucene.index.Term
import org.apache.lucene.search.BooleanClause
import org.apache.lucene.search.BooleanQuery
import org.apache.lucene.search.SortField
import org.apache.lucene.search.TermQuery

class SearchSpec {

  public static final int DEFAULT_SEARCH_BATCH_SIZE = 200

  protected BooleanQuery query

  int batchSize = DEFAULT_SEARCH_BATCH_SIZE

  protected List sortFields = []

  void query(String fieldName, value) {
    if (query == null)
      query = new BooleanQuery()
    if (value instanceof Iterable || value.getClass().isArray()) {
      def q2 = new BooleanQuery()
      for (String v in value)
        q2.add(new TermQuery(new Term(fieldName, v)), BooleanClause.Occur.SHOULD)
      query.add(q2, BooleanClause.Occur.MUST)
    } else
      query.add(new TermQuery(new Term(fieldName, value.toString())), BooleanClause.Occur.MUST)
  }

  void sort(Object[] fields) {
    fields.each { f ->
      sortFields.add(new SortField(f.toString(), SortField.Type.STRING))
    }
  }

  void sort(String fieldName, SortField.Type type) {
    sortFields.add(new SortField(fieldName, type))
  }

  void sort(String fieldName, SortField.Type type, boolean reverse) {
    sortFields.add(new SortField(fieldName, type, reverse))
  }
}
