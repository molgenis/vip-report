package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Phenopacket {
  @JsonProperty("phenotypicFeatures")
  @NonNull
  List<PhenotypicFeature> phenotypicFeaturesList;

  @JsonProperty("subject")
  @NonNull
  Individual subject;
}
