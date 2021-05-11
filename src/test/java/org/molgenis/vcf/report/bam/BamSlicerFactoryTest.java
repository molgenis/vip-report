package org.molgenis.vcf.report.bam;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BamSlicerFactoryTest {
  private BamSlicerFactory bamSlicerFactory;
  @Mock private BamStreamWriterFactory bamStreamWriterFactory;

  @BeforeEach
  void setUp() {
    bamSlicerFactory = new BamSlicerFactory(bamStreamWriterFactory);
  }

  @Test
  void create() {
    Path bamPath = Path.of("src", "test", "resources", "example.bam");
    BamSlicer bamSlicer = bamSlicerFactory.create(bamPath);
    assertNotNull(bamSlicer);
  }
}
