package edu.dal.ocrrect.word.dictionary;

import edu.dal.ocrrect.util.IOUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class ListDictionary extends AbstractDictionary {
  public ListDictionary(List<Path> paths) throws IOException {
    super(IOUtils.readList(paths));
  }
}