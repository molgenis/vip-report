package org.molgenis.vcf.report.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class BestCompressionGZIPOutputStream extends GZIPOutputStream {

  public BestCompressionGZIPOutputStream(OutputStream out) throws IOException {
    super(out);
    def.setLevel(Deflater.BEST_COMPRESSION);
  }
}
