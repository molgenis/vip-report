package org.molgenis.vcf.report.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;

@JsonSerialize(using = BytesSerializer.class)
@Value
@NonFinal
public class Bytes {

  @NonNull byte[] bytes;
}
