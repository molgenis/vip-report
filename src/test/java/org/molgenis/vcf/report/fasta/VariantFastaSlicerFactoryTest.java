package org.molgenis.vcf.report.fasta;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VariantFastaSlicerFactoryTest {

  @Mock
  private FastaSlicerFactory fastaSlicerFactory;
  @Mock
  private VariantIntervalCalculator variantIntervalCalculator;

  private VcfFastaSlicerFactory vcfFastaSlicerFactory;

  @BeforeEach
  void setUpBeforeEach() {
    vcfFastaSlicerFactory = new VcfFastaSlicerFactory(fastaSlicerFactory, variantIntervalCalculator);
  }

  @Test
  void create() {
    Path fastaGzPath = Path.of("src", "test", "resources", "example.fasta.gz");
    FastaSlicer fastaSlicer = mock(FastaSlicer.class);
    when(fastaSlicerFactory.create(fastaGzPath)).thenReturn(fastaSlicer);
    assertAll(
        () -> assertNotNull(vcfFastaSlicerFactory.create(fastaGzPath)),
        () -> verify(fastaSlicerFactory).create(fastaGzPath));
  }
}
