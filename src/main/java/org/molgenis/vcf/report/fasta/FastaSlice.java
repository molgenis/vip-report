package org.molgenis.vcf.report.fasta;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class FastaSlice {
  ContigInterval interval;
  byte[] fastaGz;
}
