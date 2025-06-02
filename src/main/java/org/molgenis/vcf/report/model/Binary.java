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

  @JsonProperty("rna")
  Map<String, Rna> rna;

  @Value
  @NonFinal
  public static class Rna {
    @JsonProperty("bw")
    Bytes bw;
    @JsonProperty("bed")
    Bytes bed;
  }

  @Value
  @NonFinal
  public static class Cram {
    @JsonProperty("cram")
    Bytes cram;
    @JsonProperty("crai")
    Bytes crai;
  }
}
