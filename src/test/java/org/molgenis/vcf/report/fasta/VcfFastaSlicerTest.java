package org.molgenis.vcf.report.fasta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VcfFastaSlicerTest {

  @Mock
  private FastaSlicer fastaSlicer;
  @Mock
  private VcfIntervalCalculator vcfIntervalCalculator;
  private VcfFastaSlicer vcfFastaSlicer;

  @BeforeEach
  void setUpBeforeEach() {
    vcfFastaSlicer = new VcfFastaSlicer(fastaSlicer, vcfIntervalCalculator);
  }

  @Test
  void generate() {
    VariantContext variantContext1 = mock(VariantContext.class);
    VariantContext variantContext2 = mock(VariantContext.class);

    ContigInterval contigInterval0 = new ContigInterval("1", 750, 1250);
    ContigInterval contigInterval1 = new ContigInterval("2", 1750, 2250);

    when(vcfIntervalCalculator.calculate(List.of(variantContext1, variantContext2), 250))
        .thenReturn(List.of(contigInterval0, contigInterval1));

    FastaSlice fastaSlice0 = mock(FastaSlice.class);
    FastaSlice fastaSlice1 = mock(FastaSlice.class);

    doReturn(fastaSlice0).when(fastaSlicer).slice(contigInterval0);
    doReturn(fastaSlice1).when(fastaSlicer).slice(contigInterval1);

    assertEquals(List.of(fastaSlice0, fastaSlice1),
        vcfFastaSlicer.generate(List.of(variantContext1, variantContext2), 250));
  }
}
