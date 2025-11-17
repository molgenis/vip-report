package org.molgenis.vcf.report.generator;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.FastaSlice;
import org.molgenis.vcf.report.fasta.FastaSlicer;
import org.molgenis.vcf.report.fasta.FastaSlicerFactory;

class FastaSlicerFactoryTest {

  private static FastaSlicer fastaSlicer;

  @BeforeAll
  static void setUpBeforeAll() {
    fastaSlicer =
        new FastaSlicerFactory().create(Paths.get("src", "test", "resources", "example.fasta.gz"));
  }

  @Test
  void encode() {
    ContigInterval interval = new ContigInterval("1", 10, 20);
    FastaSlice fastaSlice = fastaSlicer.slice(interval);
    assertAll(
        () -> assertEquals(fastaSlice.getInterval(), interval),
        () -> assertEquals(">1:10-20\nACCAGTAGCTG\n", decompress(fastaSlice.getFastaGz())));
  }

  // decode,because gzip can differ between operating systems
  private String decompress(byte[] fastaGz) {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(fastaGz))) {
      return new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
