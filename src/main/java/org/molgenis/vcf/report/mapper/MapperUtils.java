package org.molgenis.vcf.report.mapper;

import htsjdk.variant.vcf.VCFCompoundHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.metadata.Number;
import org.molgenis.vcf.report.model.metadata.Number.NumberBuilder;

public class MapperUtils {

  private MapperUtils() {
  }

  public static Number mapNumber(VCFCompoundHeaderLine vcfCompoundHeaderLine) {
    Number.Type numberType = MapperUtils.mapNumberType(vcfCompoundHeaderLine.getCountType());

    NumberBuilder numberBuilder = Number.builder();
    numberBuilder.type(numberType);
    if (numberType == Number.Type.NUMBER) {
      int count = vcfCompoundHeaderLine.getCount();
      numberBuilder.count(count);
      if (count > 1) {
        numberBuilder.separator(',');
      }
    } else {
      numberBuilder.separator(',');
    }
    return numberBuilder.build();
  }

  public static Number.Type mapNumberType(VCFHeaderLineCount countType) {
    Number.Type numberType;
    switch (countType) {
      case INTEGER:
        numberType = Number.Type.NUMBER;
        break;
      case A:
        numberType = Number.Type.PER_ALT;
        break;
      case R:
        numberType = Number.Type.PER_ALT_AND_REF;
        break;
      case G:
        numberType = Number.Type.PER_GENOTYPE;
        break;
      case UNBOUNDED:
        numberType = Number.Type.OTHER;
        break;
      default:
        throw new UnexpectedEnumException(countType);
    }
    return numberType;
  }
}
