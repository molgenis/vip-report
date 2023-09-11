package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class Items<E> {

  @JsonProperty("items")
  @JsonInclude
  @NonNull
  List<E> items;

  @JsonProperty("total")
  long total;
}
