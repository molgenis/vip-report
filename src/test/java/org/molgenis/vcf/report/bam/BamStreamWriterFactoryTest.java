package org.molgenis.vcf.report.bam;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import htsjdk.samtools.BAMStreamWriter;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BamStreamWriterFactoryTest {

  private BamStreamWriterFactory bamStreamWriterFactory;

  @BeforeEach
  void setUp() {
    bamStreamWriterFactory = new BamStreamWriterFactory();
  }

  @Test
  void create() {
    BAMStreamWriter bamStreamWriter = bamStreamWriterFactory.create(mock(OutputStream.class));
    assertAll(
        () -> assertNotNull(bamStreamWriter),
        () ->
            assertEquals(
                Deflater.BEST_COMPRESSION,
                BlockCompressedOutputStream.getDefaultCompressionLevel()));
  }
}
