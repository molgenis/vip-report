package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Base85 {

  @JsonProperty("vcfGz")
  @NonNull
  String vcfGz;
}
