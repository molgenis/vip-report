package org.molgenis.vcf.report.model.metadata;

import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import org.molgenis.vcf.report.model.Format;

@Value
@NonFinal
@SuperBuilder
public class FormatMetadata extends CompoundMetadata<Format> {

}
