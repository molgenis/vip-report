package org.molgenis.vcf.report.bedmethyl;

import htsjdk.variant.vcf.VCFFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VariantIntervalCalculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BedmethylFilterTest {

  private BedmethylFilter bedmethylFilter;
  @Mock
  private VariantIntervalCalculator variantIntervalCalculator;

  @BeforeEach
  void setUp() {
    Path bedmethylPath = Path.of("src", "test", "resources", "example.bedmethyl");
    bedmethylFilter= new BedmethylFilter(variantIntervalCalculator, bedmethylPath);
  }

  @Test
  void filter() {
    ContigInterval contigInterval = mock(ContigInterval.class);
    when(contigInterval.getContig()).thenReturn("X");
    when(contigInterval.getStart()).thenReturn(146991507);
    when(contigInterval.getStop()).thenReturn(146991550);

    VCFFileReader vcfFileReader = mock(VCFFileReader.class);

    when(variantIntervalCalculator.calculate(vcfFileReader, null, Path.of("fake"))).thenReturn(
        List.of(contigInterval));

    assertEquals(
            """
                    X\t146991507\t146991508\th\t6\t+\t146991507\t146991508\t255,0,0\t6\t0.00\t0\t0\t6\t1\t0\t0\t1
                    X\t146991507\t146991508\tm\t6\t+\t146991507\t146991508\t255,0,0\t6\t100.00\t6\t0\t0\t1\t0\t0\t1
                    X\t146991508\t146991509\th\t10\t-\t146991508\t146991509\t255,0,0\t10\t0.00\t0\t1\t9\t0\t0\t0\t0
                    X\t146991508\t146991509\tm\t10\t-\t146991508\t146991509\t255,0,0\t10\t90.00\t9\t1\t0\t0\t0\t0\t0
                    X\t146991550\t146991551\th\t2\t+\t146991550\t146991551\t255,0,0\t2\t0.00\t0\t0\t2\t3\t0\t3\t0
                    X\t146991550\t146991551\tm\t2\t+\t146991550\t146991551\t255,0,0\t2\t100.00\t2\t0\t0\t3\t0\t3\t0
                    """,
        decompress(bedmethylFilter.filter(vcfFileReader, null, Path.of("fake"))));
  }

  private static String decompress(byte[] bytes) {
    try (ByteArrayInputStream gzipInputStream = new ByteArrayInputStream(bytes)) {
      return new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}