package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NonNull;

@Data
public class Items<E> {

  @JsonProperty("items")
  @NonNull
  private List<E> items;

  @JsonProperty("total")
  @NonNull
  private long total;
}
