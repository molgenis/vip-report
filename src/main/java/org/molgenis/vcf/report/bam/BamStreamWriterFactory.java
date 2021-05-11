package org.molgenis.vcf.report.bam;

import htsjdk.samtools.BAMStreamWriter;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import org.springframework.stereotype.Component;

@Component
public class BamStreamWriterFactory {

  BamStreamWriterFactory() {}

  public BAMStreamWriter create(OutputStream outputStream) {
    BlockCompressedOutputStream.setDefaultCompressionLevel(Deflater.BEST_COMPRESSION);
    return new BAMStreamWriter(outputStream, null, null, -1, null);
  }
}
