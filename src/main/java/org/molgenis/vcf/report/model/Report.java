package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Report {

  @JsonProperty("fastaGz")
  Map<String, Bytes> fastaGz;

  @JsonProperty("genesGz")
  Bytes genesGz;

  @JsonProperty("cram")
  Map<String, Cram> cram;

  @JsonProperty("wasmBinary")
  @NonNull
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
  @NonNull
  Bytes database;
}
