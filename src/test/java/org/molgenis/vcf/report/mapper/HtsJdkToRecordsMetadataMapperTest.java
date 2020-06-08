package org.molgenis.vcf.report.mapper;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFHeader;
import htsjdk.variant.vcf.VCFInfoHeaderLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;
import org.molgenis.vcf.report.model.metadata.RecordsMetadata;

@ExtendWith(MockitoExtension.class)
class HtsJdkToRecordsMetadataMapperTest {

  @Mock
  HtsJdkToInfoMetadataMapper htsJdkToInfoMetadataMapper;
  private HtsJdkToRecordsMetadataMapper htsJdkToRecordsMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToRecordsMetadataMapper = new HtsJdkToRecordsMetadataMapper(htsJdkToInfoMetadataMapper);
  }

  @Test
  void map() {
    VCFHeader vcfHeader = mock(VCFHeader.class);
    VCFInfoHeaderLine vcfInfoHeaderLine = mock(VCFInfoHeaderLine.class);
    when(vcfHeader.getInfoHeaderLines()).thenReturn(singletonList(vcfInfoHeaderLine));
    InfoMetadata infoMetadata = mock(InfoMetadata.class);
    when(htsJdkToInfoMetadataMapper.map(vcfInfoHeaderLine)).thenReturn(infoMetadata);
    assertEquals(
        new RecordsMetadata(singletonList(infoMetadata)),
        htsJdkToRecordsMetadataMapper.map(vcfHeader));
  }
}
