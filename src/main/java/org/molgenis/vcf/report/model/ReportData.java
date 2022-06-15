package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class ReportData {

  @JsonProperty("samples")
  @JsonInclude
  @NonNull
  List<Sample> samples;

  @JsonProperty("phenotypes")
  @JsonInclude
  @NonNull
  List<Phenopacket> phenopackets;
}
