package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class InfoMetadata {
  public enum Type {
    CHARACTER,
    INTEGER,
    FLAG,
    FLOAT,
    STRING,
    NESTED
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

  @JsonProperty("source")
  String source;

  @JsonProperty("version")
  String version;

  @JsonProperty("nested")
  List<InfoMetadata> nestedMetadata;
}
