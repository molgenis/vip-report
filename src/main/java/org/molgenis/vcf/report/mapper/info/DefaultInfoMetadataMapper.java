package org.molgenis.vcf.report.mapper.info;

import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.Collections;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.mapper.MapperUtils;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.InfoMetadata.Type;
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
    InfoMetadata.Type type = mapType(vcfInfoHeaderLine.getType());
    Number number = type != Type.FLAG ? MapperUtils.mapNumber(vcfInfoHeaderLine) : null;

    return new InfoMetadata(
        id,
        number,
        type,
        vcfInfoHeaderLine.getDescription(),
        vcfInfoHeaderLine.getSource(),
        vcfInfoHeaderLine.getVersion(),
        Collections.emptyList());
  }

  private InfoMetadata.Type mapType(VCFHeaderLineType vcfHeaderLineType) {
    InfoMetadata.Type type;
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
