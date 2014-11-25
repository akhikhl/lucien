package org.akhikhl.lucien

import org.apache.lucene.analysis.core.KeywordAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LuceneIndex {

  protected static final Logger log = LoggerFactory.getLogger(LuceneIndex)

  static final luceneMethodClasses = [LuceneIndexSearcherMethods, LuceneIndexWriterMethods, LuceneIndexDocumentMethods]

  static withIndex(File indexDir, Closure closure) {
    LuceneIndex index = new LuceneIndex(indexDir)
    def result
    try {
      closure.delegate = index
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      result = closure(index)
    } finally {
      index.close()
    }
    result
  }

  private static final Version LuceneVersion = Version.LUCENE_CURRENT

  protected FSDirectory indexDir
  protected IndexReader reader
  protected IndexSearcher searcher

  LuceneIndex(File indexDir) {
    this.indexDir = FSDirectory.open(indexDir)
  }

  synchronized void close() {
    log.trace 'close'
    if (searcher != null)
      searcher = null
    if (reader != null) {
      reader.close()
      reader = null
    }
    if (indexDir != null) {
      indexDir.close()
      indexDir = null
    }
  }

  boolean exists() {
    DirectoryReader.indexExists(indexDir)
  }

  /**
   * Returns per-instance index reader. The reader is automatically closed by LuceneIndex.close.
   * @return IndexReader
   */
  synchronized IndexReader getReader() {
    if (reader == null)
      reader = openIndexReader()
    else {
      IndexReader newReader = DirectoryReader.openIfChanged(reader)
      if (newReader != null) {
        log.warn 'reopening index reader after change'
        reader.close()
        reader = newReader
      }
    }
    reader
  }

  /**
   * Returns per-instance index searcher. The searcher is automatically nullified by LuceneIndex.close.
   * @return IndexSearcher
   */
  synchronized IndexSearcher getSearcher() {
    if (searcher == null)
      searcher = new IndexSearcher(getReader())
    else {
      IndexReader newReader = DirectoryReader.openIfChanged(reader)
      if (newReader != null) {
        log.warn 'reopening index reader after change'
        reader.close()
        reader = newReader
        searcher = new IndexSearcher(reader)
      }
    }
    searcher
  }

  /**
   * Attention: if you call openIndexReader, you are responsible for closing the returned IndexReader.
   * @return IndexReader
   */
  IndexReader openIndexReader() {
    DirectoryReader.open(indexDir)
  }

  /**
   * Attention: if you call openIndexWriter, you are responsible for closing the returned IndexWriter.
   * @return IndexWriter
   */
  IndexWriter openIndexWriter() {
    IndexWriterConfig iwc = new IndexWriterConfig(LuceneVersion, new KeywordAnalyzer())
    iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND)
    new IndexWriter(indexDir, iwc)
  }

  def withReader(Closure closure) {
    IndexReader reader = openIndexReader()
    def result
    try {
      closure.delegate = reader
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      result = closure(reader)
    } finally {
      reader.close()
    }
    result
  }

  def withSearcher(Closure closure) {
    def result
    withReader { IndexReader reader ->
      IndexSearcher searcher = new IndexSearcher(reader)
      closure.delegate = searcher
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      result = closure(searcher)
    }
    result
  }

  def withWriter(Closure closure) {
    IndexWriter writer = openIndexWriter()
    def result
    try {
      closure.delegate = writer
      closure.resolveStrategy = Closure.DELEGATE_FIRST
      result = closure(writer)
    } finally {
      writer.close()
    }
    result
  }
}
