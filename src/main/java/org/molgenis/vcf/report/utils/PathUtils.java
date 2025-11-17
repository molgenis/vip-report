package org.molgenis.vcf.report.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathUtils {

  private PathUtils() {
  }

  public static List<Path> parsePaths(String optionValue) {
    List<Path> result = new ArrayList<>();
    String[] paths = optionValue.split(",");
    for (String path : paths) {
      result.add(Path.of(path));
    }
    return result;
  }

  public static String getNameWithoutVcfExtensions(Path path) {
    String filename = path.getFileName().toString();
    if (filename.endsWith(".vcf.gz")) {
      return filename.substring(0, filename.length() - 7);
    } else if (filename.endsWith(".vcf")) {
      return filename.substring(0, filename.length() - 4);
    }
    return filename;
  }

  public static String getDatabaseLocation(Path inputPath) {
    Path parentDir = inputPath.getParent();
    return String.format("%s.db",
        Path.of(String.valueOf(parentDir), getNameWithoutVcfExtensions(inputPath)));
  }
}
