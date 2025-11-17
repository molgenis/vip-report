package org.molgenis.vcf.report.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

// Workaround for https://github.com/samtools/htsjdk/issues/1718
public class VcfInputStreamDecorator {

  public static final String TR_ALLELE = "<CNV:TR>";
  public static final String FORMAT = "<CNV:TR%d>";

  private VcfInputStreamDecorator() {}

  public static InputStream preprocessVCF(File inputVCF) throws IOException {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
        InputStream inputStream = new FileInputStream(inputVCF);
        BufferedReader reader =
            inputVCF.toPath().toString().endsWith(".vcf.gz")
                ? new BufferedReader(new InputStreamReader(new GZIPInputStream(inputStream)))
                : new BufferedReader(new InputStreamReader(inputStream)); ) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = processLine(line);
        writer.write(line);
        writer.newLine();
      }
      writer.flush();
      return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
  }

  private static String processLine(String line) {
    if (line.startsWith("#") || line.isEmpty()) {
      return line;
    }
    String[] parts = line.split("\t");
    if (parts.length >= 5) {
      String altField = parts[4];
      if (altField.contains(TR_ALLELE)) {
        String[] alts = altField.split(",");
        if (alts.length > 1) {
          replaceStrAlleles(alts, parts);
        }
      }
    } else {
      throw new InvalidVcfLineException(line);
    }
    return String.join("\t", parts);
  }

  private static void replaceStrAlleles(String[] alts, String[] parts) {
    int i = 1;
    List<String> newAlt = new ArrayList<>();
    for (String alt : alts) {
      if (alt.equals(TR_ALLELE)) {
        newAlt.add(String.format(FORMAT, i));
        ++i;
      }
    }
    parts[4] = String.join(",", newAlt);
  }
}
