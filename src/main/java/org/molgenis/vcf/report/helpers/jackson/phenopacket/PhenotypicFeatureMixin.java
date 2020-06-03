package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PhenotypicFeatureMixin extends PhenopacketModelMixin {
  @JsonIgnore
  Object getUnknownFields();

  @JsonIgnore
  Object getDescriptor();

  @JsonIgnore
  Object getOnsetCase();

  @JsonIgnore
  Object getDescription();

  @JsonIgnore
  Object getDescriptionBytes();

  @JsonIgnore
  Object getTypeOrBuilder();

  @JsonIgnore
  Object getNegated();

  @JsonIgnore
  Object getSeverity();

  @JsonIgnore
  Object getSeverityOrBuilder();

  @JsonIgnore
  Object getModifiers();

  @JsonIgnore
  Object getModifiersCount();

  @JsonIgnore
  Object getModifiersList();

  @JsonIgnore
  Object getModifiersOrBuilderList();

  @JsonIgnore
  Object getModifiersOrBuilder();

  @JsonIgnore
  Object getAgeOfOnset();

  @JsonIgnore
  Object getAgeOfOnsetOrBuilder();

  @JsonIgnore
  Object getAgeRangeOfOnset();

  @JsonIgnore
  Object getAgeRangeOfOnsetOrBuilder();

  @JsonIgnore
  Object getClassOfOnset();

  @JsonIgnore
  Object getClassOfOnsetOrBuilder();

  @JsonIgnore
  Object getEvidenceList();

  @JsonIgnore
  Object getEvidenceOrBuilderList();

  @JsonIgnore
  Object getEvidenceCount();

  @JsonIgnore
  Object getEvidence();

  @JsonIgnore
  Object getEvidenceOrBuilder();

  @JsonIgnore
  Object getSerializedSize();

  @JsonIgnore
  Object getDefaultInstance();

  @JsonIgnore
  Object getParserForType();

  @JsonIgnore
  Object getDefaultInstanceForType();
}
