package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.experimental.SuperBuilder;
import org.molgenis.vcf.report.model.Info;

@SuperBuilder
public class InfoMetadata extends CompoundMetadata<Info> {

  @JsonProperty("source")
  String source;

  @JsonProperty("version")
  String version;
}
