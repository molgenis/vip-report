package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface OntologyClassMixin extends PhenopacketModelMixin {
  @JsonIgnore
  Object getUnknownFields();

  @JsonIgnore
  Object getDescriptor();

  @JsonIgnore
  Object getIdBytes();

  @JsonIgnore
  Object getLabelBytes();

  @JsonIgnore
  Object getSerializedSize();

  @JsonIgnore
  Object getDefaultInstance();

  @JsonIgnore
  Object getParserForType();

  @JsonIgnore
  Object getDefaultInstanceForType();
}
