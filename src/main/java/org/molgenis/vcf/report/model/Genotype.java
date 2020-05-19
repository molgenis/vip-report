package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class Genotype {

  @JsonProperty("a")
  @NonNull
  private List<String> alleles;

  @JsonProperty("p")
  @NonNull
  private boolean phased;

  @JsonProperty("t")
  @NonNull
  private Type type;

  public enum Type {
    /** heterozygous */
    @JsonProperty("het")
    HETEROZYGOUS,
    /** homozygous (all alleles are non-reference) */
    @JsonProperty("hom_a")
    HOMOZYGOUS_ALT,
    /** homozygous (all alleles are reference) */
    @JsonProperty("hom_r")
    HOMOZYGOUS_REF,
    /** no calls were made at all loci, */
    @JsonProperty("miss")
    NO_CALL,
    /** no calls were made at some loci, */
    @JsonProperty("part")
    PARTIAL_CALL
  }
}
