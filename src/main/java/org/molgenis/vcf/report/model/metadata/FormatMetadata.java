package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class FormatMetadata {

  public enum Type {
    CHARACTER,
    INTEGER,
    FLOAT,
    STRING,
  }

  @JsonProperty("id")
  @NonNull
  String id;

  @JsonProperty("number")
  Number number;

  @JsonProperty("type")
  @NonNull
  Type type;

  @JsonProperty("description")
  @NonNull
  String description;
}
