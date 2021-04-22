package org.molgenis.vcf.report.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class BestCompressionGZIPOutputStream extends GZIPOutputStream {

  public BestCompressionGZIPOutputStream(OutputStream out) throws IOException {
    super(out);
    def.setLevel(Deflater.BEST_COMPRESSION);
  }

  public static byte[] compress(byte[] bytes) {
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
