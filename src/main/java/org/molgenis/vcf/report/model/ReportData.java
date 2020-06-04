package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.phenopackets.schema.v1.Phenopacket;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.Pedigree.Person;

@Value
@NonFinal
public class ReportData {

  @JsonProperty("htsFile")
  @NonNull
  HtsFile file;

  @JsonProperty("persons")
  @NonNull
  Items<Person> persons;

  @JsonProperty("phenotypes")
  @NonNull
  Items<Phenopacket> phenopackets;

  @JsonProperty("records")
  @NonNull
  Items<Record> records;
}
