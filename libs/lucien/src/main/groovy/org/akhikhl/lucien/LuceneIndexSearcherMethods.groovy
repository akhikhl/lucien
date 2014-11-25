package org.akhikhl.lucien
import org.apache.lucene.document.Document
import org.apache.lucene.search.*

class LuceneIndexSearcherMethods {

  protected static class SearchIterator implements Iterator<Document> {

    private final IndexSearcher searcher
    private final Query query
    private final int batchSize
    private final Sort sort
    private Document[] bufferedResult
    private int index
    private ScoreDoc lastHit

    protected SearchIterator(IndexSearcher searcher, Query query, int batchSize, Sort sort) {
      this.searcher = searcher
      this.query = query
      this.batchSize = batchSize
      this.sort = sort
    }

    @Override
    boolean hasNext() {
      if (bufferedResult == null || index == bufferedResult.length)
        queryNext()
      return index < bufferedResult.length
    }

    @Override
    Document next() {
      if (bufferedResult == null || index == bufferedResult.length)
        queryNext()
      assert index < bufferedResult.length
      bufferedResult[index++]
    }

    private void queryNext() {
      index = 0
      ScoreDoc[] hits = searcher.searchAfter(lastHit, query, batchSize, sort).scoreDocs
      bufferedResult = new Document[hits.length]
      for (int i = 0; i < hits.length; i++) {
        bufferedResult[i] = searcher.doc(hits[i].doc)
        if (i == hits.length - 1)
          lastHit = hits[i]
      }
    }
  }

  protected static class SearchIterable implements Iterable<Document> {

    private final IndexSearcher searcher
    private final Query query
    private final int batchSize
    private final Sort sort

    protected SearchIterable(IndexSearcher searcher, Query query, int batchSize, Sort sort) {
      this.searcher = searcher
      this.query = query
      this.batchSize = batchSize
      this.sort = sort
    }

    @Override
    Iterator<Document> iterator() {
      new SearchIterator(searcher, query, batchSize, sort)
    }
  }

  static Iterable<Document> findDocs(IndexSearcher searcher, Map queryMap, Closure configureClosure = null) {

    def spec = new SearchSpec()

    queryMap.each { key, value ->
      spec.query key, value
    }

    if (configureClosure != null) {
      configureClosure.delegate = spec
      configureClosure.resolveStrategy = Closure.DELEGATE_FIRST
      configureClosure()
    }

    Query query = spec.query ?: new MatchAllDocsQuery()

    Sort sort
    if (spec.sortFields)
      sort = new Sort(spec.sortFields as SortField[])
    else
      sort = Sort.INDEXORDER

    findDocs(searcher, query, spec.batchSize, sort)
  }

  static Iterable<Document> findDocs(IndexSearcher searcher, Query query, int batchSize, Sort sort) {
    new SearchIterable(searcher, query, batchSize, sort)
  }

  static Document getDoc(IndexSearcher searcher, Map queryMap, Closure configureClosure = null) {

    def spec = new SearchSpec()

    queryMap.each { key, value ->
      spec.query key, value
    }

    if (configureClosure != null) {
      configureClosure.delegate = spec
      configureClosure.resolveStrategy = Closure.DELEGATE_FIRST
      configureClosure()
    }

    Query query = spec.query ?: new MatchAllDocsQuery()

    getDoc(searcher, query)
  }

  static Document getDoc(IndexSearcher searcher, Query query) {
    TopScoreDocCollector collector = TopScoreDocCollector.create(1, true)
    searcher.search(query, collector)
    ScoreDoc[] hits = collector.topDocs().scoreDocs
    if (hits.length != 0)
      return searcher.doc(hits[0].doc)
    return null
  }

  static int numDocs(IndexSearcher searcher, Map queryMap, Closure configureClosure = null) {

    def spec = new SearchSpec()

    queryMap.each { key, value ->
      spec.query key, value
    }

    if (configureClosure != null) {
      configureClosure.delegate = spec
      configureClosure.resolveStrategy = Closure.DELEGATE_FIRST
      configureClosure()
    }

    Query query = spec.query ?: new MatchAllDocsQuery()

    numDocs(searcher, query)
  }

  static int numDocs(IndexSearcher searcher, Query query) {
    searcher.search(query, 1).totalHits
  }
}
