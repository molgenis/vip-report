package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PhenopacketModelMixin {
  @JsonIgnore
  Object getParserForType();
  @JsonIgnore
  Object getDefaultInstanceForType();
  @JsonIgnore
  Object isInitialized();
}
