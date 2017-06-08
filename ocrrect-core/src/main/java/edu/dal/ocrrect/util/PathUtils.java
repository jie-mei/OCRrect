package edu.dal.ocrrect.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PathUtils {
  public static Path TEMP_DIR = Paths.get("tmp");

  private PathUtils() {}

  public static Path getTempPath(String pathname) {
    Path path = TEMP_DIR.resolve(pathname);
    try {
      Files.createDirectories(path.getParent());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return path;
  }

  public static Path getTempFile() throws IOException {
    if (! Files.exists(TEMP_DIR)) {
      Files.createDirectories(TEMP_DIR);
    }
    Path temp = Files.createTempFile(TEMP_DIR, null, ".tmp");
    temp.toFile().deleteOnExit();
    return temp;
  }

  /**
   * Retrieve all subpaths in the given pathname and filter by a wildcard. Retrieved paths are
   * sorted by their pathnames.
   *
   * @param path the resource folder in the file system.
   * @param glob a wildcard pattern for filtering the retrieved subpaths.
   * @return a list of subpaths.
   * @throws IOException if I/O error occurs.
   */
  public static List<Path> listPaths(Path path, String glob) throws IOException {
    List<Path> paths = new ArrayList<>();
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(path, glob)) {
      for (Path p : ds) {
        paths.add(p);
      }
    }
    paths.sort((a, b) -> a.getFileName().compareTo(b.getFileName()));
    return paths;
  }
}
