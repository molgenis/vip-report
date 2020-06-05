package org.molgenis.vcf.report.mapper;

import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import java.util.HashMap;
import java.util.Map;
import org.molgenis.vcf.report.model.Items;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.HtsFile.HtsFormat;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.springframework.stereotype.Component;

@Component
public class HtsFileMapper {

  private static final String BUILD_GRCH_38 = "GRCh38";
  private static final String BUILD_GRCH_37 = "GRCh37";
  private static final String BUILD_GRCH_36 = "GRCh36";
  private static final String BUILD_HG_19 = "hg19";
  private static final String BUILD_HG_18 = "hg18";
  private static final String BUILD_B_38 = "b38";
  private static final String BUILD_B_37 = "b37";
  private static final String BUILD_B_36 = "b36";
  private static final String ASSEMBLY_FIELD = "assembly";

  public HtsFile map(VCFHeader fileHeader, String inputFile) {
    String genomeBuild = parseGenomeBuild(fileHeader);
    return HtsFile.newBuilder()
        .setUri(inputFile)
        .setGenomeAssembly(genomeBuild)
        .setHtsFormat(HtsFormat.VCF)
        .build();
  }

  private String parseGenomeBuild(VCFHeader fileHeader) {
    String genomeBuild = "Unknown";
    if (!fileHeader.getContigLines().isEmpty()) {
      VCFContigHeaderLine contig = fileHeader.getContigLines().get(0);
      Map<String, String> fields = contig.getGenericFields();
      if (fields.containsKey(ASSEMBLY_FIELD)) {
        String assembly = fields.get(ASSEMBLY_FIELD);
        if (assembly.equalsIgnoreCase(BUILD_B_36)
            || assembly.startsWith(BUILD_GRCH_36)
            || assembly.equals(BUILD_HG_18)) {
          genomeBuild = BUILD_GRCH_36;
        } else if (assembly.equalsIgnoreCase(BUILD_B_37)
            || assembly.startsWith(BUILD_GRCH_37)
            || assembly.equals(BUILD_HG_19)) {
          genomeBuild = BUILD_GRCH_37;
        } else if (assembly.equalsIgnoreCase(BUILD_B_38) || assembly.startsWith(BUILD_GRCH_38)) {
          genomeBuild = BUILD_GRCH_38;
        }
      }
    }
    return genomeBuild;
  }
}
