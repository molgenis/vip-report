package org.molgenis.vcf.report.mapper;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import htsjdk.variant.variantcontext.VariantContext;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.report.model.Record;

@ExtendWith(MockitoExtension.class)
class HtsJdkToRecordsMapperTest {

  @Mock private HtsJdkToRecordMapper htsJdkToRecordMapper;
  private HtsJdkToRecordsMapper htsJdkToRecordsMapper;

  @BeforeEach
  void setUpBeforeEach() {
    htsJdkToRecordsMapper = new HtsJdkToRecordsMapper(htsJdkToRecordMapper);
  }

  @Test
  void map() {
    VariantContext variantContext0 = mock(VariantContext.class);
    VariantContext variantContext1 = mock(VariantContext.class);
    Iterable<VariantContext> variantContexts =
        List.of(variantContext0, variantContext1, mock(VariantContext.class));

    int maxNrRecords = 2;
    Record record0 = mock(Record.class);
    Record record1 = mock(Record.class);
    List<Record> records = List.of(record0, record1);
    Items<Record> expectedRecordItems = new Items<>(records, 3);
    doReturn(record0).when(htsJdkToRecordMapper).map(variantContext0, emptyList());
    doReturn(record1).when(htsJdkToRecordMapper).map(variantContext1, emptyList());
    assertEquals(
        expectedRecordItems, htsJdkToRecordsMapper.map(variantContexts, maxNrRecords, emptyList()));
  }
}
