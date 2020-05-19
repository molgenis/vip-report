package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class ReportData {

  @JsonProperty("samples")
  @NonNull
  Items<Sample> samples;

  @JsonProperty("records")
  @NonNull
  Items<Record> records;
}
