package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class ReportMetadata {

  @JsonProperty("app")
  @NonNull
  AppMetadata appMetadata;

  @JsonProperty("htsFile")
  @NonNull
  HtsFile htsFile;
}
