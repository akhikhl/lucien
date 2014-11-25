package org.akhikhl.lucien

import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter

class LuceneIndexWriterMethods {

  static void addDocument(IndexWriter writer, Closure closure) {
    Document doc = new Document()
    closure.delegate = doc
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure()
    writer.addDocument(doc)
  }
}
