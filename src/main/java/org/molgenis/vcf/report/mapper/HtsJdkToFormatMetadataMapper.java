package org.molgenis.vcf.report.mapper;

import htsjdk.variant.vcf.VCFFormatHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineType;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.metadata.FormatMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.springframework.stereotype.Component;

@Component
public class HtsJdkToFormatMetadataMapper {

  public FormatMetadata map(VCFFormatHeaderLine vcfFormatHeaderLine) {
    String id = vcfFormatHeaderLine.getID();
    Number number = MapperUtils.mapNumber(vcfFormatHeaderLine);
    FormatMetadata.Type type = mapType(vcfFormatHeaderLine.getType());
    String description = vcfFormatHeaderLine.getDescription();
    return new FormatMetadata(id, number, type, description);
  }

  private FormatMetadata.Type mapType(VCFHeaderLineType vcfHeaderLineType) {
    FormatMetadata.Type type;
    switch (vcfHeaderLineType) {
      case Integer:
        type = FormatMetadata.Type.INTEGER;
        break;
      case Float:
        type = FormatMetadata.Type.FLOAT;
        break;
      case String:
        type = FormatMetadata.Type.STRING;
        break;
      case Character:
        type = FormatMetadata.Type.CHARACTER;
        break;
      default:
        throw new UnexpectedEnumException(vcfHeaderLineType);
    }
    return type;
  }
}
