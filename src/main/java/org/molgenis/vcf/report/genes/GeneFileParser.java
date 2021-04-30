package org.molgenis.vcf.report.genes;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneFileParser {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeneFileParser.class);

  private GeneFileParser(){}

  public static List<GeneLine> readGeneFile(Path inputGenesFile) {
    List<GeneLine> geneLines;
    try (InputStream fileStream = new FileInputStream(inputGenesFile.toFile());
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader reader = new BufferedReader(new InputStreamReader(gzipStream))) {
      CsvToBean<GeneLine> csvToBean =
          new CsvToBeanBuilder<GeneLine>(reader)
              .withSeparator('\t')
              .withType(GeneLine.class)
              .withThrowExceptions(false)
              .build();
      geneLines = csvToBean.parse();
      handleCsvParseExceptions(csvToBean.getCapturedExceptions());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return geneLines;
  }

  static void handleCsvParseExceptions(List<CsvException> exceptions) {
    exceptions.forEach(
        csvException -> {
          // ignore errors parsing trailing comment lines
          if (!(csvException.getLine()[0].startsWith("#"))) {
            LOGGER.error(
                String.format("%s,%s", csvException.getLineNumber(), csvException.getMessage()));
          }
        });
  }
}
