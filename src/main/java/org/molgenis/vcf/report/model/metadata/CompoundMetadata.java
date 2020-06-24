package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import org.molgenis.vcf.report.model.Compound;

@Value
@NonFinal
@SuperBuilder
public class CompoundMetadata<T extends Compound> {

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

  @JsonProperty("nested")
  List<CompoundMetadata<T>> nestedMetadata;
}
