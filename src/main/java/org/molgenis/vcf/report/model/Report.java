package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.jspecify.annotations.Nullable;

@Value
@NonFinal
public class Report {

  @JsonProperty("fastaGz")
  @Nullable Map<String, Bytes> fastaGz;

  @JsonProperty("genesGz")
  @Nullable Bytes genesGz;

  @JsonProperty("cram")
  Map<String, Cram> cram;

  @JsonProperty("wasmBinary")
  Bytes wasmBinary;

  @Value
  @NonFinal
  public static class Cram {
    @JsonProperty("cram")
    Bytes cram;

    @JsonProperty("crai")
    Bytes crai;
  }

  @JsonProperty("database")
  Bytes database;
}
