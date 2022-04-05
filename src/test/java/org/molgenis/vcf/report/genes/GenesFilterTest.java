package org.molgenis.vcf.report.genes;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;

@ExtendWith(MockitoExtension.class)
class GenesFilterTest {

  private GenesFilter genesFilter;
  @Mock
  private VcfIntervalCalculator vcfIntervalCalculator;

  @BeforeEach
  void setUp() {
    Path genesGzPath = Path.of("src", "test", "resources", "exampleGene.txt.gz");
    genesFilter = new GenesFilter(vcfIntervalCalculator, genesGzPath);
  }

  @Test
  void filter() {
    VariantContext variantContext = mock(VariantContext.class);
    Iterable<VariantContext> variants = singletonList(variantContext);
    ContigInterval contigInterval = mock(ContigInterval.class);
    when(contigInterval.getContig()).thenReturn("1");
    when(contigInterval.getStart()).thenReturn(661200);
    when(contigInterval.getStop()).thenReturn(661700);
    when(vcfIntervalCalculator.calculate(variants, 250)).thenReturn(singletonList(contigInterval));

    assertEquals(
        "590\tNR_028327.1\tchr1\t-\t661138\t665731\t665731\t665731\t3\t661138,665277,665562,\t665184,665335,665731,\t0\tLOC100133331\tnone\tnone\t-1,-1,-1,\n",
        decompress(genesFilter.filter(variants, 250)));
  }

  @Test
  void filterPrefixedBuild() {
    VariantContext variantContext = mock(VariantContext.class);
    Iterable<VariantContext> variants = singletonList(variantContext);
    ContigInterval contigInterval = mock(ContigInterval.class);
    when(contigInterval.getContig()).thenReturn("chr1");
    when(contigInterval.getStart()).thenReturn(661200);
    when(contigInterval.getStop()).thenReturn(661700);
    when(vcfIntervalCalculator.calculate(variants, 250)).thenReturn(singletonList(contigInterval));

    assertEquals(
        "590\tNR_028327.1\tchr1\t-\t661138\t665731\t665731\t665731\t3\t661138,665277,665562,\t665184,665335,665731,\t0\tLOC100133331\tnone\tnone\t-1,-1,-1,\n",
        decompress(genesFilter.filter(variants, 250)));
  }

  private static String decompress(byte[] bytes) {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      return new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}