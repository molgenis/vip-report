package org.molgenis.vcf.report.genes;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GeneFileParserTest {

  @BeforeEach
  void restLoggingEvents() {
    TestAppender.reset();
  }

  @Test
  void readGeneFile() {
    Path genesGzPath = Path.of("src", "test", "resources", "exampleGene.txt.gz");
    GeneLine expectedGeneLine =
        new GeneLine(
            585,
            "NR_046018.2",
            "chr1",
            '+',
            11873,
            14409,
            14409,
            14409,
            3,
            "11873,12612,13220,",
            "12227,12721,14409,",
            0,
            "DDX11L1",
            "none",
            "none",
            "-1,-1,-1,");

    List<GeneLine> actual = GeneFileParser.readGeneFile(genesGzPath);

    assertAll(
        () -> assertEquals(22, actual.size()), () -> assertEquals(expectedGeneLine, actual.get(0)));
  }

  @Test
  void handleCsvParseExceptions() {
    CsvRequiredFieldEmptyException csvParsingException = mock(CsvRequiredFieldEmptyException.class);
    when(csvParsingException.getLine())
        .thenReturn(new String[] {"This", "is", "a", "test", "line"});
    GeneFileParser.handleCsvParseExceptions(Collections.singletonList(csvParsingException));
    assertEquals(0, TestAppender.events.size());
  }
}
