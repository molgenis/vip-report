package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.molgenis.vcf.utils.sample.model.Phenopacket;
import org.molgenis.vcf.utils.sample.model.Sample;

@Value
@NonFinal
public class ReportData {

  @JsonProperty("samples")
  @JsonInclude
  List<Sample> samples;

  @JsonProperty("phenotypes")
  @JsonInclude
  List<Phenopacket> phenopackets;
}
