package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Binary {

  @JsonProperty("vcf")
  @NonNull
  Bytes vcf;

  @JsonProperty("fastaGz")
  Map<String, Bytes> fastaGz;

  @JsonProperty("genesGz")
  Bytes genesGz;

  @JsonProperty("bam")
  Map<String, Bytes> bam;
}
