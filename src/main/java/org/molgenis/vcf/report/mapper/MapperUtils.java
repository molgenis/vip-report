package org.molgenis.vcf.report.mapper;

import htsjdk.variant.vcf.VCFCompoundHeaderLine;
import htsjdk.variant.vcf.VCFHeaderLineCount;
import org.molgenis.vcf.report.UnexpectedEnumException;
import org.molgenis.vcf.report.model.metadata.Number;

public class MapperUtils {

  private MapperUtils() {
  }

  public static Number mapNumber(VCFCompoundHeaderLine vcfCompoundHeaderLine) {
    Number.Type numberType = MapperUtils.mapNumberType(vcfCompoundHeaderLine.getCountType());
    Integer count;
    if (numberType == Number.Type.NUMBER) {
      count = vcfCompoundHeaderLine.getCount();
    } else {
      count = null;
    }
    return new Number(numberType, count, ',');
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
