package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class AppMetadata {

  @JsonProperty("name")
  @NonNull
  String appName;

  @JsonProperty("version")
  @NonNull
  String appVersion;

  @JsonProperty("args")
  @NonNull
  String appArguments;

}
