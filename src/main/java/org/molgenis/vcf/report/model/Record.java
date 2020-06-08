package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Record {

  @JsonProperty("c")
  @NonNull
  String chromosome;

  @JsonProperty("p")
  @NonNull
  int position;

  @JsonProperty("i")
  List<String> identifiers;

  @JsonProperty("r")
  @NonNull
  String referenceAllele;

  @JsonProperty("a")
  @NonNull
  List<String> alternateAlleles;

  @JsonProperty("q")
  Double quality;

  @JsonProperty("f")
  List<String> filterStatus;

  @JsonProperty("n")
  Info info;

  @JsonProperty("s")
  List<RecordSample> recordSamples;
}
