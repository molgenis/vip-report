package org.molgenis.vcf.report.bam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;

@ExtendWith(MockitoExtension.class)
class VcfBamSlicerTest {
  @Mock private BamSlicer bamSlicer;
  @Mock private VcfIntervalCalculator vcfIntervalCalculator;
  private VcfBamSlicer vcfBamSlicer;

  @BeforeEach
  void setUp() {
    vcfBamSlicer = new VcfBamSlicer(bamSlicer, vcfIntervalCalculator);
  }

  @Test
  void generate() {
    String sampleId = "sample0";
    int flanking = 250;

    VariantContext variantContext1 = mock(VariantContext.class);
    VariantContext variantContext2 = mock(VariantContext.class);

    ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
    ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);

    when(vcfIntervalCalculator.calculate(
            List.of(variantContext1, variantContext2), flanking, sampleId))
        .thenReturn(List.of(contigInterval0, contigInterval1));
    BamSlice bamSlice = mock(BamSlice.class);
    when(bamSlicer.slice(List.of(contigInterval0, contigInterval1))).thenReturn(bamSlice);
    assertEquals(
        bamSlice,
        vcfBamSlicer.generate(List.of(variantContext1, variantContext2), flanking, sampleId));
  }
}
