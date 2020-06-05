package org.molgenis.vcf.report.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFContigHeaderLine;
import htsjdk.variant.vcf.VCFHeader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.report.model.Items;
import org.phenopackets.schema.v1.core.HtsFile;
import org.phenopackets.schema.v1.core.HtsFile.HtsFormat;
import org.phenopackets.schema.v1.core.Pedigree.Person;
import org.phenopackets.schema.v1.core.Pedigree.Person.AffectedStatus;

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

    Map<String, String> samples = new HashMap<>();
    samples.put("John","John");
    samples.put("Jimmy","Jimmy");
    HtsFile expected = HtsFile.newBuilder().setHtsFormat(HtsFormat.VCF).setGenomeAssembly("GRCh38").setUri("test.vcf").putAllIndividualToSampleIdentifiers(samples).build();
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

    Map<String, String> samples = new HashMap<>();
    samples.put("John","John");
    samples.put("Jimmy","Jimmy");
    HtsFile expected = HtsFile.newBuilder().setHtsFormat(HtsFormat.VCF).setGenomeAssembly("GRCh37").setUri("test.vcf").putAllIndividualToSampleIdentifiers(samples).build();
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

    Map<String, String> samples = new HashMap<>();
    samples.put("John","John");
    samples.put("Jimmy","Jimmy");
    HtsFile expected = HtsFile.newBuilder().setHtsFormat(HtsFormat.VCF).setGenomeAssembly("GRCh36").setUri("test.vcf").putAllIndividualToSampleIdentifiers(samples).build();
    assertEquals(expected, htsFileMapper.map(header, "test.vcf"));
  }
}