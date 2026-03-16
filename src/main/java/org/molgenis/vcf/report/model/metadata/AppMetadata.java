package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class AppMetadata {

  @JsonProperty("name")
  String appName;

  @JsonProperty("version")
  String appVersion;

  @JsonProperty("args")
  String appArguments;
}
