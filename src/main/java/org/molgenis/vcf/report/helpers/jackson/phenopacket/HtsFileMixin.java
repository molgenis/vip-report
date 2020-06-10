package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HtsFileMixin extends PhenopacketModelMixin {
  @JsonIgnore
  Object getUnknownFields();

  @JsonIgnore
  Object getDescriptor();

  @JsonIgnore
  Object getUriBytes();

  @JsonIgnore
  Object getDescriptionBytes();

  @JsonIgnore
  Object getHtsFormatValue();

  @JsonIgnore
  Object getGenomeAssemblyBytes();

  @JsonIgnore
  Object getIndividualToSampleIdentifiersMap();

  @JsonIgnore
  Object getIndividualToSampleIdentifiersOrDefault();

  @JsonIgnore
  Object getIndividualToSampleIdentifiersOrThrow();

  @JsonIgnore
  Object getSerializedSize();

  @JsonIgnore
  Object getDefaultInstance();
}
