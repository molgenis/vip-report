package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Report {

  @JsonProperty("binary")
  @NonNull
  Binary binary;

  @JsonProperty("database")
  @NonNull
  Bytes database;
}
