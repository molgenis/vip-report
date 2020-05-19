package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Report {

  @JsonProperty("metadata")
  @NonNull
  ReportMetadata reportMetadata;

  @JsonProperty("data")
  @NonNull
  ReportData reportData;
}
