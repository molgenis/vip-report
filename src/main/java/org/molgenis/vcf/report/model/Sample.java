package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.phenopackets.schema.v1.core.Pedigree;

@Value
@NonFinal
public class Sample {
  @JsonProperty("person")
  @NonNull
  Pedigree.Person person;

  @JsonProperty("index")
  @NonNull
  Integer index;
}
