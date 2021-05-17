package org.molgenis.vcf.report.bam;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import htsjdk.samtools.BAMStreamWriter;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileHeader.SortOrder;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.fasta.ContigInterval;

@ExtendWith(MockitoExtension.class)
class BamSlicerTest {

  @Mock private SamReader samReader;
  @Mock private BamStreamWriterFactory bamStreamWriterFactory;
  private BamSlicer bamSlicer;

  @BeforeEach
  void setUp() {
    bamSlicer = new BamSlicer(samReader, bamStreamWriterFactory);
  }

  @AfterEach
  void tearDown() throws Exception {
    bamSlicer.close();
  }

  @Test
  void slice() {
    String contig = "1";
    int start = 100;
    int stop = 200;

    SAMFileHeader fileHeader = mock(SAMFileHeader.class);
    int contigIndex = 1;
    when(fileHeader.getSequenceIndex(contig)).thenReturn(contigIndex);
    when(samReader.getFileHeader()).thenReturn(fileHeader);

    SAMRecord samRecord = mock(SAMRecord.class);
    when(samReader.queryOverlapping(
            new QueryInterval[] {new QueryInterval(contigIndex, start, stop)}))
        .thenReturn(new MySamRecordIterator(samRecord));

    BAMStreamWriter bamStreamWriter = mock(BAMStreamWriter.class);
    when(bamStreamWriterFactory.create(any())).thenReturn(bamStreamWriter);

    List<ContigInterval> contigIntervals = List.of(new ContigInterval(contig, start, stop));
    BamSlice bamSlice = bamSlicer.slice(contigIntervals);

    assertAll(
        () -> assertEquals(contigIntervals, bamSlice.getIntervals()),
        () -> verify(bamStreamWriter).writeHeader(fileHeader),
        () -> verify(bamStreamWriter).writeAlignment(samRecord),
        () -> verify(bamStreamWriter).finish(false));
  }

  private static class MySamRecordIterator implements SAMRecordIterator {
    private final Iterator<SAMRecord> samRecordIterator;

    public MySamRecordIterator(SAMRecord samRecord) {
      samRecordIterator = List.of(samRecord).iterator();
    }

    @Override
    public SAMRecordIterator assertSorted(SortOrder sortOrder) {
      return this;
    }

    @Override
    public void close() {
      // no op
    }

    @Override
    public boolean hasNext() {
      return samRecordIterator.hasNext();
    }

    @Override
    public SAMRecord next() {
      return samRecordIterator.next();
    }
  }
}
