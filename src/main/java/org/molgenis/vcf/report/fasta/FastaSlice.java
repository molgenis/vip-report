package org.molgenis.vcf.report.fasta;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class FastaSlice {

  @NonNull ContigInterval interval;
  @NonNull byte[] fastaGz;
}
