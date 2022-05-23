package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class ReportData {

  @JsonProperty("samples")
  @NonNull
  List<Sample> samples;

  @JsonProperty("phenotypes")
  @NonNull
  List<Phenopacket> phenopackets;
}
