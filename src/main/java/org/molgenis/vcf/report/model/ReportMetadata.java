package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.phenopackets.schema.v1.core.HtsFile;

@Value
@NonFinal
public class ReportMetadata {

  @JsonProperty("appName")
  @NonNull
  String appName;

  @JsonProperty("appVersion")
  @NonNull
  String appVersion;

  @JsonProperty("appArgs")
  @NonNull
  String appArguments;

  @JsonProperty("htsFile")
  @NonNull
  HtsFile htsFile;
}
