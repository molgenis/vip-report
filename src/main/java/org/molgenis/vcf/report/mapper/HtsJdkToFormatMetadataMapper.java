package org.molgenis.vcf.report.mapper;

import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToFormatMetadataMapper {

  public FormatMetadata map(VCFFormatHeaderLine vcfFormatHeaderLine) {
    String id = vcfFormatHeaderLine.getID();
    Number number = MapperUtils.mapNumber(vcfFormatHeaderLine);
    CompoundMetadata.Type type = mapType(vcfFormatHeaderLine.getType());
    String description = vcfFormatHeaderLine.getDescription();

    return FormatMetadata.builder()
        .id(id)
        .number(number)
        .type(type)
        .description(description)
        .build();
  }

  private CompoundMetadata.Type mapType(VCFHeaderLineType vcfHeaderLineType) {
    CompoundMetadata.Type type;
    switch (vcfHeaderLineType) {
      case Integer:
        type = CompoundMetadata.Type.INTEGER;
        break;
      case Float:
        type = CompoundMetadata.Type.FLOAT;
        break;
      case String:
        type = CompoundMetadata.Type.STRING;
        break;
      case Character:
        type = CompoundMetadata.Type.CHARACTER;
        break;
      default:
        throw new UnexpectedEnumException(vcfHeaderLineType);
    }
    return type;
  }
}
