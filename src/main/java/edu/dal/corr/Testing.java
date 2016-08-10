package edu.dal.corr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import edu.dal.corr.util.ResourceUtils;
import edu.stanford.nlp.io.IOUtils;

public class Testing {
  public static void main(String[] args) throws IOException
  {
    ResourceUtils.FIVEGRAM.stream()
        .map(Path::toString)
        .forEach(System.out::println);
  }
}
