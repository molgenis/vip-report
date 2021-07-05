package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Base85 {

  @JsonProperty("vcfGz")
  @NonNull
  String vcfGz;

  @JsonProperty("fastaGz")
  Map<String, String> fastaGz;

  @JsonProperty("genesGz")
  String genesGz;

  @JsonProperty("bam")
  Map<String, String> bam;

  @JsonProperty("decisionTree")
  String decisionTree;
}
