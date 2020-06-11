package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class HtsFile {

  @JsonProperty("uri")
  @NonNull
  String uri;

  @JsonProperty("htsFormat")
  @NonNull
  String htsFormat;

  @JsonProperty("genomeAssembly")
  @NonNull
  String genomeAssembly;
}
