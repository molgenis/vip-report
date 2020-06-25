package org.molgenis.vcf.report.mapper.info;

import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.mapper.MapperUtils;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata;
import org.molgenis.vcf.report.model.metadata.CompoundMetadata.Type;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.Number;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order
public class DefaultInfoMetadataMapper implements InfoMetadataMapper {

  @Override
  public boolean canMap(VCFInfoHeaderLine vcfInfoHeaderLine) {
    return true;
  }

  public InfoMetadata map(VCFInfoHeaderLine vcfInfoHeaderLine) {
    String id = vcfInfoHeaderLine.getID();
    CompoundMetadata.Type type = mapType(vcfInfoHeaderLine.getType());
    Number number = type != Type.FLAG ? MapperUtils.mapNumber(vcfInfoHeaderLine) : null;

    return InfoMetadata.builder()
        .id(id)
        .number(number)
        .type(type)
        .description(vcfInfoHeaderLine.getDescription())
        .source(vcfInfoHeaderLine.getSource())
        .version(vcfInfoHeaderLine.getVersion())
        .build();
  }

  private CompoundMetadata.Type mapType(VCFHeaderLineType vcfHeaderLineType) {
    CompoundMetadata.Type type;
    switch (vcfHeaderLineType) {
      case Integer:
        type = Type.INTEGER;
        break;
      case Float:
        type = Type.FLOAT;
        break;
      case String:
        type = Type.STRING;
        break;
      case Character:
        type = Type.CHARACTER;
        break;
      case Flag:
        type = Type.FLAG;
        break;
      default:
        throw new UnexpectedEnumException(vcfHeaderLineType);
    }
    return type;
  }
}
