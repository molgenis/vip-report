package org.molgenis.vcf.report.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.molgenis.vcf.report.utils.BestCompressionGZIPOutputStream;
import org.springframework.stereotype.Component;

@Component
public class BinaryEncoder {

  public byte[] encode(Path path) {
    byte[] bytes;

    if (path.toString().toLowerCase().endsWith(".gz")) {
      bytes = decompress(path);
    } else {
      bytes = toBytes(path);
    }

    return compress(bytes);
  }

  private static byte[] toBytes(Path gzPath) {
    try {
      return Files.readAllBytes(gzPath);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static byte[] decompress(Path gzPath) {
    byte[] bytes;
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(Files.newInputStream(gzPath))) {
      bytes = gzipInputStream.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bytes;
  }

  private static byte[] compress(byte[] bytes) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (GZIPOutputStream gzipOutputStream =
        new BestCompressionGZIPOutputStream(byteArrayOutputStream)) {
      gzipOutputStream.write(bytes);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return byteArrayOutputStream.toByteArray();
  }
}
