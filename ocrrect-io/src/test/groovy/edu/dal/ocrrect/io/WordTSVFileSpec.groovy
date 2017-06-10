package edu.dal.ocrrect.io

import edu.dal.ocrrect.util.ResourceUtils
import edu.dal.ocrrect.util.Word
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification;

class WordTSVFileSpec extends Specification {
  @Rule
  TemporaryFolder tempFolder
  @Shared
  def words = [new Word(0, "", "", "", "", "Hello", ",", "world", "!"),
               new Word(5, "", "", "", "Hello", ",", "world", "!", ""),
               new Word(7, "", "", "Hello", ",", "world", "!", "", ""),
               new Word(12, "", "Hello", ",", "world", "!", "", "", "")]

  def "test read word TSV file"() {
    setup:
    def tsvWords = new WordTSVFile(ResourceUtils.getResource("test.words.tsv")).read()

    expect:
    tsvWords[idx].position() == words[idx].position()
    tsvWords[idx].context() == words[idx].context()

    where:
    idx << (0..(words.size() - 1))
  }

  def "test write word TSV file"() {
    setup:
    def tsv = new WordTSVFile(tempFolder.newFile().toPath())
    tsv.write(words)

    expect:
    tsv.read() == words
  }
}
