package org.molgenis.vcf.report.bam;

import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.molgenis.vcf.report.fasta.ContigInterval;

@Value
@NonFinal
public class BamSlice {

  @NonNull List<ContigInterval> intervals;
  @NonNull byte[] bam;
}
