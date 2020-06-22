package org.molgenis.vcf.report.mapper.info;

import htsjdk.variant.vcf.VCFHeaderLineCount;
import htsjdk.variant.vcf.VCFHeaderLineType;
import htsjdk.variant.vcf.VCFInfoHeaderLine;

public class InfoMetadataUtils {
  private InfoMetadataUtils() {}

  static void validateNumberUnbounded(VCFInfoHeaderLine vcfInfoHeaderLine) {
    VCFHeaderLineCount countType = vcfInfoHeaderLine.getCountType();
    if (countType != VCFHeaderLineCount.UNBOUNDED) {
      String number =
          countType == VCFHeaderLineCount.INTEGER
              ? String.valueOf(vcfInfoHeaderLine.getCount())
              : countType.toString();

      throw new IllegalArgumentException(
          String.format(
              "Expected ##INFO=<ID=%s,Number=%s,...> to be of Number '%s'",
              vcfInfoHeaderLine.getID(), number, '.'));
    }
  }

  static void validateTypeString(VCFInfoHeaderLine vcfInfoHeaderLine) {
    VCFHeaderLineType type = vcfInfoHeaderLine.getType();
    if (type != VCFHeaderLineType.String) {
      throw new IllegalArgumentException(
          String.format(
              "Expected ##INFO=<ID=%s,...,Type=%s,...> to be of type %s",
              vcfInfoHeaderLine.getID(), type, VCFHeaderLineType.String));
    }
  }
}
