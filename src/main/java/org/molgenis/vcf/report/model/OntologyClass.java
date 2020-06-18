package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class OntologyClass {
  @JsonProperty("id")
  @NonNull
  String id;

  @JsonProperty("label")
  @NonNull
  String label;
}
