package org.molgenis.vcf.report.bam;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;

@ExtendWith(MockitoExtension.class)
class VcfBamSlicerFactoryTest {
  @Mock private BamSlicerFactory bamSlicerFactory;
  @Mock private VcfIntervalCalculator vcfIntervalCalculator;
  private VcfBamSlicerFactory vcfBamSlicerFactory;

  @BeforeEach
  void setUp() {
    vcfBamSlicerFactory = new VcfBamSlicerFactory(bamSlicerFactory, vcfIntervalCalculator);
  }

  @Test
  void create() {
    Path bamPath = Path.of("src", "test", "resources", "example.bam");
    BamSlicer bamSlicer = mock(BamSlicer.class);
    when(bamSlicerFactory.create(bamPath)).thenReturn(bamSlicer);
    assertNotNull(vcfBamSlicerFactory.create(bamPath));
  }
}
