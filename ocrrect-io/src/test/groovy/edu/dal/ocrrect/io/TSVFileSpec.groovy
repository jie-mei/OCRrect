package edu.dal.ocrrect.io

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.OpenOption

class TSVFileSpec extends Specification {
  @Rule
  TemporaryFolder tempFolder

  def "get path from object"() {
    setup:
      def path = tempFolder.newFile().toPath()
      def tsvFile = new TSVFile(path) {
        @Override
        List read() throws IOException {}
        @Override
        void write(List elements, OpenOption... options) throws IOException {}
      }

    expect:
      tsvFile.path() == path
  }
}
