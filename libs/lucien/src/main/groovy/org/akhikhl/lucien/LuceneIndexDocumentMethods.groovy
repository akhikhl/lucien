package org.akhikhl.lucien

import org.apache.lucene.document.*
import org.apache.lucene.index.IndexableField

class LuceneIndexDocumentMethods {

  static Map asMap(Document doc) {
    doc.getFields().collectEntries { f ->
      [f.name(), f.stringValue()]
    }
  }

  static void field(Document doc, IndexableField field) {
    doc.add(field)
  }

  static void field(Document doc, String fieldName, String fieldValue, Field.Store store = Field.Store.YES) {
    doc.add(new StringField(fieldName, fieldValue, store))
  }

  static void field(Document doc, String fieldName, int fieldValue, Field.Store store = Field.Store.YES) {
    doc.add(new IntField(fieldName, fieldValue, store))
  }

  static void field(Document doc, String fieldName, long fieldValue, Field.Store store = Field.Store.YES) {
    doc.add(new LongField(fieldName, fieldValue, store))
  }

  static void field(Document doc, String fieldName, float fieldValue, Field.Store store = Field.Store.YES) {
    doc.add(new FloatField(fieldName, fieldValue, store))
  }

  static void field(Document doc, String fieldName, double fieldValue, Field.Store store = Field.Store.YES) {
    doc.add(new DoubleField(fieldName, fieldValue, store))
  }

  static void field(Document doc, String fieldName, boolean fieldValue, Field.Store store = Field.Store.YES) {
    doc.add(new IntField(fieldName, fieldValue ? 1 : 0, store))
  }
}
