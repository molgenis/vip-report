package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class RecordSample {

  @JsonProperty("gt")
  Genotype genotype;

  // additional genotype data
  @JsonProperty("f")
  Map<String, Object> dataMap;
}
