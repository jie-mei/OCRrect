package edu.dal.ocrrect.io

import edu.dal.ocrrect.util.ResourceUtils
import edu.dal.ocrrect.util.Token
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification;

class TokenTSVFileSpec extends Specification {
  @Rule
  TemporaryFolder tempFolder
  @Shared
  def tk = [new Token("Hello", 0),
            new Token(",", 5),
            new Token("world", 7),
            new Token("!", 12)]

  def "test read token TSV file"() {
    setup:
      def tsv = new TokenTSVFile(ResourceUtils.getResource("test.tokens.tsv"))

    expect:
      tsv.read() == tk
  }

  def "test write token TSV file"() {
    setup:
      def tsv = new TokenTSVFile(tempFolder.newFile().toPath())
      tsv.write(tk)

    expect:
      tsv.read() == tk
  }
}
