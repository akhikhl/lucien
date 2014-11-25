package org.akhikhl.lucien

import spock.lang.Specification

class LuceneIndexTest extends Specification {

  private File testDir
  private LuceneIndex index

  def setup() {
    testDir = new File(System.getProperty('java.io.tmpdir'), UUID.randomUUID().toString())
    index = new LuceneIndex(testDir)
    index.withWriter {
      addDocument {
        field 'fruit', 'apple'
        field 'color', 'red'
      }
      addDocument {
        field 'fruit', 'apple'
        field 'color', 'green'
      }
      addDocument {
        field 'fruit', 'blueberry'
        field 'color', 'blue'
      }
    }
  }

  def cleanup() {
    index.close()
    testDir.deleteDir()
  }

  def 'should support simple search'() {
  setup:
    List docs = index.searcher.findDocs(fruit: 'apple', {
      sort 'fruit', 'color'
    }).asList()
  expect:
    docs.size() == 2
    docs[0].fruit == 'apple'
    docs[0].color == 'green'
    docs[1].fruit == 'apple'
    docs[1].color == 'red'
  }

  def 'should support AND-search on multiple criteria'() {
  setup:
    List docs = index.searcher.findDocs(fruit: 'apple', color: 'red').asList()
  expect:
    docs.size() == 1
    docs[0].fruit == 'apple'
    docs[0].color == 'red'
  }

  def 'should support OR-search on multiple values'() {
  setup:
    List docs = index.searcher.findDocs(fruit: ['apple', 'blueberry'], {
      sort 'fruit', 'color'
    }).asList()
  expect:
    docs.size() == 3
    docs[0].fruit == 'apple'
    docs[0].color == 'green'
    docs[1].fruit == 'apple'
    docs[1].color == 'red'
    docs[2].fruit == 'blueberry'
    docs[2].color == 'blue'
  }

  def 'should return empty document set when search does not find anything'() {
  setup:
    List docs = index.searcher.findDocs(fruit: 'orange').asList()
  expect:
    docs.size() == 0
  }

  def 'should support paged iteration'() {
  setup:
    List docs = index.searcher.findDocs([:], {
      batchSize = 2
      sort 'fruit', 'color'
    }).asList()
  expect:
    docs.size() == 3
    docs[0].fruit == 'apple'
    docs[0].color == 'green'
    docs[1].fruit == 'apple'
    docs[1].color == 'red'
    docs[2].fruit == 'blueberry'
    docs[2].color == 'blue'
  }
}

