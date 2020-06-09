package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PersonMixin extends PhenopacketModelMixin {
  @JsonIgnore
  Object isInitialized();

  @JsonIgnore
  Object getUnknownFields();

  @JsonIgnore
  Object getDescriptor();

  @JsonIgnore
  Object getFamilyIdBytes();

  @JsonIgnore
  Object getIndividualIdBytes();

  @JsonIgnore
  Object getPaternalIdBytes();

  @JsonIgnore
  Object getMaternalIdBytes();

  @JsonIgnore
  Object getSerializedSize();

  @JsonIgnore
  Object getDefaultInstance();
}
