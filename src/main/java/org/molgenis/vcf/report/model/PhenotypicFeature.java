package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class PhenotypicFeature {
  @JsonProperty("type")
  @NonNull
  OntologyClass ontologyClass;
}
