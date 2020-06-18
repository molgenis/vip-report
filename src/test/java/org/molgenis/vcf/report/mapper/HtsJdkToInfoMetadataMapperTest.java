package org.molgenis.vcf.report.mapper;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.vcf.VCFInfoHeaderLine;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.mapper.info.InfoMetadataMapper;
import org.molgenis.vcf.report.model.metadata.InfoMetadata;

@ExtendWith(MockitoExtension.class)
class HtsJdkToInfoMetadataMapperTest {
  @Mock InfoMetadataMapper infoMetadataMapper;
  @Mock InfoMetadataMapper defaultInfoMetadataMapper;
  private HtsJdkToInfoMetadataMapper htsJdkToInfoMetadataMapper;

  @BeforeEach
  void setUpBeforeEach() {
    List<InfoMetadataMapper> infoMetadataMappers =
        asList(infoMetadataMapper, defaultInfoMetadataMapper);
    htsJdkToInfoMetadataMapper = new HtsJdkToInfoMetadataMapper(infoMetadataMappers);
  }

  @Test
  void map() {
    VCFInfoHeaderLine vcfHeaderInfoLine = mock(VCFInfoHeaderLine.class);
    when(defaultInfoMetadataMapper.canMap(vcfHeaderInfoLine)).thenReturn(true);
    InfoMetadata infoMetadata = mock(InfoMetadata.class);
    when(defaultInfoMetadataMapper.map(vcfHeaderInfoLine)).thenReturn(infoMetadata);
    assertEquals(infoMetadata, htsJdkToInfoMetadataMapper.map(vcfHeaderInfoLine));
  }

  @Test
  void mapUnsupported() {
    VCFInfoHeaderLine vcfHeaderInfoLine = mock(VCFInfoHeaderLine.class);
    assertThrows(
        UnsupportedOperationException.class, () -> htsJdkToInfoMetadataMapper.map(vcfHeaderInfoLine));
  }
}
