package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.molgenis.vcf.report.model.metadata.ReportMetadata;

@Value
@NonFinal
public class Report {

  @JsonProperty("metadata")
  @NonNull
  ReportMetadata reportMetadata;

  @JsonProperty("data")
  @NonNull
  ReportData reportData;

  @JsonProperty("binary")
  @NonNull
  Binary binary;

  @JsonProperty("decisionTree")
  Map<?,?> decisionTree;

  @JsonProperty("sampleTree")
  Map<?,?> sampleTree;

  @JsonProperty("vcfMeta")
  @NonNull
  Map<?,?> vcfMeta;

  @JsonProperty("config")
  Map<?,?> templateConfig;
}
