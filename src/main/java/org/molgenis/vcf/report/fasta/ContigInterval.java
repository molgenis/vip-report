package org.molgenis.vcf.report.fasta;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class ContigInterval {

  String contig;
  int start;
  int stop;
}
