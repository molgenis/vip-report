package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class Record {

  @JsonProperty("c")
  @NonNull
  private String chromosome;

  @JsonProperty("p")
  @NonNull
  private int position;

  @JsonProperty("i")
  private List<String> identifiers;

  @JsonProperty("r")
  @NonNull
  private String referenceAllele;

  @JsonProperty("a")
  @NonNull
  private List<String> alternateAlleles;

  @JsonProperty("q")
  private Double quality;

  @JsonProperty("f")
  private List<String> filterStatus;

  @JsonProperty("s")
  private List<RecordSample> recordSamples;
}
