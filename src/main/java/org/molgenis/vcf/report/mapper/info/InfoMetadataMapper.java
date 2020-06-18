package org.molgenis.vcf.report.mapper.info;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;

public interface InfoMetadataMapper {

  boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine);

  InfoMetadata map(VCFInfoHeaderLine vcfInfoHeaderLine);
}
