package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class RecordSample {

  @JsonProperty("gt")
  @NonNull
  private Genotype genotype;
}
