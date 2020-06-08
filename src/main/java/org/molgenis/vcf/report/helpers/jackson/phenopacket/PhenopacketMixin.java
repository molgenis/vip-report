package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface PhenopacketMixin extends PhenopacketModelMixin {
  @JsonIgnore
  Object getUnknownFields();

  @JsonIgnore
  Object getDescriptor();

  @JsonIgnore
  Object getIdBytes();

  @JsonIgnore
  Object getSubjectOrBuilder();

  @JsonIgnore
  Object getPhenotypicFeaturesOrBuilderList();

  @JsonIgnore
  Object getPhenotypicFeaturesOrBuilder();

  @JsonIgnore
  Object getBiosamplesList();

  @JsonIgnore
  Object getBiosamplesOrBuilderList();

  @JsonIgnore
  Object getBiosamplesCount();

  @JsonIgnore
  Object getBiosamples();

  @JsonIgnore
  Object getBiosamplesOrBuilder();

  @JsonIgnore
  Object getGenesList();

  @JsonIgnore
  Object getGenesOrBuilderList();

  @JsonIgnore
  Object getGenesCount();

  @JsonIgnore
  Object getGenes();

  @JsonIgnore
  Object getGenesOrBuilder();

  @JsonIgnore
  Object getVariantsList();

  @JsonIgnore
  Object getVariantsOrBuilderList();

  @JsonIgnore
  Object getVariantsCount();

  @JsonIgnore
  Object getVariants();

  @JsonIgnore
  Object getVariantsOrBuilder();

  @JsonIgnore
  Object getDiseasesList();

  @JsonIgnore
  Object getDiseasesOrBuilderList();

  @JsonIgnore
  Object getDiseasesCount();

  @JsonIgnore
  Object getDiseases();

  @JsonIgnore
  Object getDiseasesOrBuilder();

  @JsonIgnore
  Object getHtsFilesList();

  @JsonIgnore
  Object getHtsFilesOrBuilderList();

  @JsonIgnore
  Object getHtsFilesCount();

  @JsonIgnore
  Object getHtsFiles();

  @JsonIgnore
  Object getHtsFilesOrBuilder();

  @JsonIgnore
  Object getMetaData();

  @JsonIgnore
  Object getMetaDataOrBuilder();

  @JsonIgnore
  Object getSerializedSize();

  @JsonIgnore
  Object getDefaultInstance();

}
