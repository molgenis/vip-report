package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.phenopackets.schema.v1.Phenopacket;

@Value
@NonFinal
public class ReportData {

  @JsonProperty("samples")
  @NonNull
  Items<Sample> samples;

  @JsonProperty("phenotypes")
  @NonNull
  Items<Phenopacket> phenopackets;

  @JsonProperty("records")
  @NonNull
  Items<Record> records;
}
