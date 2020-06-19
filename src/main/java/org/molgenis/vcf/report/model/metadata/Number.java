package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Number {
  public enum Type {
    /** fixed number exposed through InfoMetadata.number) */
    NUMBER,
    /** one value per alternate allele */
    PER_ALT,
    /** one value for each possible allele (including the reference) */
    PER_ALT_AND_REF,
    /** one value for each possible genotype */
    PER_GENOTYPE,
    /** number of possible values varies, is unknown, or is unbounded */
    OTHER
  }

  @JsonProperty("type")
  @NonNull
  Type type;

  @JsonProperty("count")
  Integer count;

  @JsonIgnore char separator;
}
