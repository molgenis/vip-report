package org.molgenis.vcf.report.fasta;

import htsjdk.samtools.CRAMFileReader;
import htsjdk.samtools.cram.ref.ReferenceSource;
import java.nio.file.Path;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.springframework.stereotype.Component;

@Component
public class CramReaderFactory {

  public CRAMFileReader create(SampleSettings.CramPath cramPath, Path reference) {
    return new CRAMFileReader(
        cramPath.getCram().toFile(), cramPath.getCrai().toFile(), new ReferenceSource(reference));
  }
}
