package edu.dal.corr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

public class MappedBufferTesting {
  public static void main(String[] args) throws IOException
  {
    Path path = ResourceUtils.TEST_INPUT_SEGMENT;
//    System.out.println(String.join("\n", Files.readAllLines(path)));
//    System.out.println();

    List<Long> linePos = new ArrayList<>();
//    try (RandomAccessFile raf = new RandomAccessFile(
//        ResourceUtils.TEST_INPUT_SEGMENT.toFile(), "r")) {
//      String line = null;
//      while ((line = raf.readLine()) != null) {
//        long pos = raf.getFilePointer();
//        linePos.add(pos);
//        System.out.println(line + " " + pos);
//      }
//    }
    try (
      FileChannel fc = FileChannel.open(path, StandardOpenOption.READ);
      BufferedReader br = new BufferedReader(
          new InputStreamReader(Channels.newInputStream(fc)));
    ){
      String line = null;
      while ((line = br.readLine()) != null) {
        long pos = fc.position();
        linePos.add(pos);
        System.out.println(line + " " + pos);
      }
    }
    System.out.println(linePos);

//    try (BufferedReader br = IOUtils.getBufferedFileReader(
//        ResourceUtils.TEST_INPUT_SEGMENT.toString())) {
//      String line = null;
//      while ((line = br.readLine()) != null) {
//        System.out.println(line);
//      }
//    }

    int readSize = 1024;
    try (FileChannel fc = FileChannel.open(ResourceUtils.TEST_INPUT_SEGMENT)) {
      long strPos = 0;
      for (int i = 0; i < linePos.size(); i++) {
        long endPos = linePos.get(i);
        MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, strPos, endPos - strPos);
        while (mbb.hasRemaining()) {
          byte[] bytes = new byte[mbb.remaining()];
          mbb.get(bytes);
          System.out.println("NEW LINE: (" + strPos + "," + endPos + ")" + new String(bytes, "UTF-8"));
        }
        strPos = endPos;
      }
    }
  }
}
