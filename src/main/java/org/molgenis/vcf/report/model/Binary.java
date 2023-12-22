package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Binary {

  @JsonProperty("vcf")
  @NonNull
  Bytes vcf;

  @JsonProperty("fastaGz")
  Map<String, Bytes> fastaGz;

  @JsonProperty("genesGz")
  Bytes genesGz;

  @JsonProperty("cram")
  Map<String, Cram> cram;

  @JsonProperty("bedmethyl")
  Map<String, Bytes> bedmethyl;

  @Value
  @NonFinal
  public static class Cram {
    @JsonProperty("cram")
    Bytes cram;
    @JsonProperty("crai")
    Bytes crai;
  }
}
