package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IndividualMixin extends PhenopacketModelMixin {
  @JsonIgnore
  Object getUnknownFields();

  @JsonIgnore
  Object getDescriptor();

  @JsonIgnore
  Object getAgeCase();

  @JsonIgnore
  Object getIdBytes();

  @JsonIgnore
  Object getAlternateIdsList();

  @JsonIgnore
  Object getAlternateIdsCount();

  @JsonIgnore
  Object getAlternateIds();

  @JsonIgnore
  Object getAlternateIdsBytes();

  @JsonIgnore
  Object hasDateOfBirth();

  @JsonIgnore
  Object getDateOfBirth();

  @JsonIgnore
  Object getDateOfBirthOrBuilder();

  @JsonIgnore
  Object hasAgeAtCollection();

  @JsonIgnore
  Object getAgeAtCollection();

  @JsonIgnore
  Object getAgeAtCollectionOrBuilder();

  @JsonIgnore
  Object hasAgeRangeAtCollection();

  @JsonIgnore
  Object getAgeRangeAtCollection();

  @JsonIgnore
  Object getAgeRangeAtCollectionOrBuilder();

  @JsonIgnore
  Object getSexValue();

  @JsonIgnore
  Object getSex();

  @JsonIgnore
  Object getKaryotypicSexValue();

  @JsonIgnore
  Object getKaryotypicSex();

  @JsonIgnore
  Object hasTaxonomy();

  @JsonIgnore
  Object getTaxonomy();

  @JsonIgnore
  Object getTaxonomyOrBuilder();

  @JsonIgnore
  Object getSerializedSize();

  @JsonIgnore
  Object getDefaultInstance();
}
