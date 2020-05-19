package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
public class Sample {

  @JsonProperty("name")
  @NonNull
  private String name;
}
