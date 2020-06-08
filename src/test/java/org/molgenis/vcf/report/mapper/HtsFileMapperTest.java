package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.HtsFile.HtsFormat;

@ExtendWith(MockitoExtension.class)
class HtsFileMapperTest {

  @Test
  void map() {
    HtsFileMapper htsFileMapper = new HtsFileMapper();
    VCFHeader header = mock(VCFHeader.class);
    VCFContigHeaderLine contig = mock(VCFContigHeaderLine.class);
    Map<String, String> contigMap = new HashMap<>();
    contigMap.put("assembly", "GRCh38.5");
    when(contig.getGenericFields()).thenReturn(contigMap);
    when(header.getContigLines()).thenReturn(Collections.singletonList(contig));

    HtsFile expected = HtsFile.newBuilder().setHtsFormat(HtsFormat.VCF).setGenomeAssembly("GRCh38").setUri("test.vcf").build();
    assertEquals(expected, htsFileMapper.map(header, "test.vcf"));
  }

  @Test
  void mapB37() {
    HtsFileMapper htsFileMapper = new HtsFileMapper();
    VCFHeader header = mock(VCFHeader.class);
    VCFContigHeaderLine contig = mock(VCFContigHeaderLine.class);
    Map<String, String> contigMap = new HashMap<>();
    contigMap.put("assembly", "hg19");
    when(contig.getGenericFields()).thenReturn(contigMap);
    when(header.getContigLines()).thenReturn(Collections.singletonList(contig));

    HtsFile expected = HtsFile.newBuilder().setHtsFormat(HtsFormat.VCF).setGenomeAssembly("GRCh37").setUri("test.vcf").build();
    assertEquals(expected, htsFileMapper.map(header, "test.vcf"));
  }

  @Test
  void mapB36() {
    HtsFileMapper htsFileMapper = new HtsFileMapper();
    VCFHeader header = mock(VCFHeader.class);
    VCFContigHeaderLine contig = mock(VCFContigHeaderLine.class);
    Map<String, String> contigMap = new HashMap<>();
    contigMap.put("assembly", "B36");
    when(contig.getGenericFields()).thenReturn(contigMap);
    when(header.getContigLines()).thenReturn(Collections.singletonList(contig));

    HtsFile expected = HtsFile.newBuilder().setHtsFormat(HtsFormat.VCF).setGenomeAssembly("NCBI36").setUri("test.vcf").build();
    assertEquals(expected, htsFileMapper.map(header, "test.vcf"));
  }
}