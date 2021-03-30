package org.molgenis.vcf.report.generator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BinaryEncoderTest {

  private BinaryEncoder binaryEncoder;

  @BeforeEach
  void setUpBeforeEach() {
    binaryEncoder = new BinaryEncoder();
  }

  @Test
  void encode() {
    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf");
    byte[] bytes = binaryEncoder.encode(inputVcfPath);
    assertTrue(decompress(bytes).startsWith("##fileformat=VCFv4.2"));
  }

  @Test
  void encodeGz() {
    Path inputVcfPath = Paths.get("src", "test", "resources", "example.vcf.gz");
    byte[] bytes = binaryEncoder.encode(inputVcfPath);
    assertTrue(decompress(bytes).startsWith("##fileformat=VCFv4.2"));
  }

  private static String decompress(byte[] bytes) {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      return new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
