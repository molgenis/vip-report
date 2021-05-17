package org.molgenis.vcf.report.bam;

import static java.util.Objects.requireNonNull;

import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class BamSlicerFactory {
  private final BamStreamWriterFactory bamStreamWriterFactory;

  BamSlicerFactory(BamStreamWriterFactory bamStreamWriterFactory) {
    this.bamStreamWriterFactory = requireNonNull(bamStreamWriterFactory);
  }

  public BamSlicer create(Path bamPath) {
    SamReader samReader = createSamReader(bamPath);
    return new BamSlicer(samReader, bamStreamWriterFactory);
  }

  private static SamReader createSamReader(Path bamPath) {
    SamReaderFactory samReaderFactory = SamReaderFactory.makeDefault();
    return samReaderFactory.open(bamPath);
  }
}
