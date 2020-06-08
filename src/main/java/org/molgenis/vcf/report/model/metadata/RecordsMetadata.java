package org.molgenis.vcf.report.model.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class RecordsMetadata {

  @JsonProperty("info")
  @NonNull
  List<InfoMetadata> infoMetadataMap;
}
