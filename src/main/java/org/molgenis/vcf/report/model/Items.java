package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Items<E> {

  @JsonProperty("items")
  @NonNull
  List<E> items;

  @JsonProperty("total")
  @NonNull
  long total;
}
