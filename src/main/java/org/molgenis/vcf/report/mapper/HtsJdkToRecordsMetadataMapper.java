package org.molgenis.vcf.report.mapper;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import htsjdk.variant.vcf.VCFHeader;
import java.util.List;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.RecordsMetadata;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToRecordsMetadataMapper {

  private final HtsJdkToInfoMetadataMapper htsJdkToInfoMetadataMapper;
  private HtsJdkToFormatMetadataMapper htsJdkToFormatMetadataMapper;

  HtsJdkToRecordsMetadataMapper(
      HtsJdkToInfoMetadataMapper htsJdkToInfoMetadataMapper,
      HtsJdkToFormatMetadataMapper htsJdkToFormatMetadataMapper) {
    this.htsJdkToInfoMetadataMapper = requireNonNull(htsJdkToInfoMetadataMapper);
    this.htsJdkToFormatMetadataMapper = requireNonNull(htsJdkToFormatMetadataMapper);
  }

  public RecordsMetadata map(VCFHeader vcfHeader) {
    List<InfoMetadata> infoMetadataList =
        vcfHeader.getInfoHeaderLines().stream()
            .map(htsJdkToInfoMetadataMapper::map)
            .collect(toList());
    List<FormatMetadata> formatMetadataList =
        vcfHeader.getFormatHeaderLines().stream()
            .map(htsJdkToFormatMetadataMapper::map)
            .collect(toList());
    return new RecordsMetadata(infoMetadataList, formatMetadataList);
  }
}
