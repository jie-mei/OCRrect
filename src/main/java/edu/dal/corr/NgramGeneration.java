package edu.dal.corr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dal.corr.util.IOUtils;

public class NgramGeneration
{
  private static Logger LOG = Logger.getLogger(NgramGeneration.class);

  public static void preprocessNgram(Path inDir, Path outDir)
    throws IOException
  {
    Files.createDirectories(outDir);

    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(inDir,
        "5gm-[0-9][0-9][0-9][0-9]")) {
      for (Path p : ds) {
        paths.add(p);
      }
    }
    paths.sort((a, b) -> a.getFileName().compareTo(b.getFileName())); 
    LOG.info("Total number of files: " + paths.size());

    BufferedWriter bw = null;
    int inputIdx = 0;
    int outputIdx = 0;
    String prev = null;
    for (Path p : paths) {
      LOG.info(String.format("(%d/%d) Processing: %s",
          ++inputIdx, paths.size(), p.getFileName()));

      try (BufferedReader br = IOUtils.newBufferedReader(p)) {
        String line; 
        while ((line = br.readLine()) != null) {
          // Extract the first word on the line.
          String curr = null;
          for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == ' ') {
              curr = line.substring(0, i);
              break;
            }
          }
          // Check and write to file.
          if (curr.length() == 0) {
            throw new RuntimeException();
          }
          if (prev == null || (! curr.equals(prev))) {
            if (prev != null) {
              bw.close();
            }
            // Write `curr` to the first line of the file.
            bw = IOUtils.newBufferedWriter(
                outDir.resolve(String.format("%010d.5gm", outputIdx++)),
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            bw.write(curr + "\n");
            bw.write(line + "\n");
            prev = curr;
          }
          if (prev != null && curr.equals(prev)) {
            bw.write(line + "\n");
          }
        }
      }
    }
  }

  public static void main(String[] args)
      throws IOException
  {
    Path fiveGm = Paths.get("/home/default/data/Google_Web_1T_5-gram/5gms");
    Path outDir = Paths.get("tmp/5gms");

    preprocessNgram(fiveGm, outDir);
  }
}
