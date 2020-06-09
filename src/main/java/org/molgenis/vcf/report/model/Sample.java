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

  // index of the sample in the VCF, -1 means the sample is not available in the file.
  @JsonProperty("index")
  @NonNull
  int index;
}
