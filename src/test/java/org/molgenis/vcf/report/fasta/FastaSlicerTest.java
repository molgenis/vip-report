package org.molgenis.vcf.report.fasta;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FastaSlicerTest {

  @Mock private ReferenceSequenceFile referenceSequenceFile;
  private FastaSlicer fastaSlicer;

  @BeforeEach
  void setUp() {
    fastaSlicer = new FastaSlicer(referenceSequenceFile);
  }

  @Test
  void slice() {
    ReferenceSequence referenceSequence = mock(ReferenceSequence.class);
    when(referenceSequence.getContigIndex()).thenReturn(0);
    when(referenceSequence.getBases()).thenReturn("ACTG".getBytes(StandardCharsets.UTF_8));
    when(referenceSequenceFile.getSubsequenceAt("1", 100, 200)).thenReturn(referenceSequence);

    ContigInterval contigInterval = new ContigInterval("1", 100, 200);
    FastaSlice fastaSlice = fastaSlicer.slice(contigInterval);
    assertAll(
        () -> assertEquals(contigInterval, fastaSlice.getInterval()),
        () -> assertEquals(">1:100-200\nACTG\n", decompress(fastaSlice.getFastaGz())));
  }

  private static String decompress(byte[] bytes) {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      return new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
