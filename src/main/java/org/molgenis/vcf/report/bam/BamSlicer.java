package org.molgenis.vcf.report.bam;

import static java.util.Objects.requireNonNull;

import htsjdk.samtools.BAMStreamWriter;
import htsjdk.samtools.QueryInterval;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.List;
import org.molgenis.vcf.report.fasta.ContigInterval;

public class BamSlicer implements AutoCloseable {

  private final SamReader samReader;
  private final BamStreamWriterFactory bamStreamWriterFactory;

  public BamSlicer(SamReader samReader, BamStreamWriterFactory bamStreamWriterFactory) {
    this.samReader = requireNonNull(samReader);
    this.bamStreamWriterFactory = requireNonNull(bamStreamWriterFactory);
  }

  public BamSlice slice(List<ContigInterval> intervals) {
    SAMFileHeader bamHeader = samReader.getFileHeader();
    QueryInterval[] queryIntervals = toQueryIntervals(intervals, bamHeader);

    SAMRecordIterator samRecordIterator = samReader.queryOverlapping(queryIntervals);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    write(bamHeader, samRecordIterator, byteArrayOutputStream);
    return new BamSlice(intervals, byteArrayOutputStream.toByteArray());
  }

  private void write(
      SAMFileHeader bamHeader, SAMRecordIterator samRecordIterator, OutputStream outputStream) {
    BAMStreamWriter bamStreamWriter = bamStreamWriterFactory.create(outputStream);
    try {
      bamStreamWriter.writeHeader(bamHeader);
      samRecordIterator.forEachRemaining(bamStreamWriter::writeAlignment);
    } finally {
      bamStreamWriter.finish(false);
    }
  }

  @Override
  public void close() throws Exception {
    samReader.close();
  }

  private static QueryInterval[] toQueryIntervals(
      List<ContigInterval> intervals, SAMFileHeader bamHeader) {
    return intervals.stream()
        .map(interval -> toQueryInterval(interval, bamHeader))
        .toArray(QueryInterval[]::new);
  }

  private static QueryInterval toQueryInterval(ContigInterval interval, SAMFileHeader bamHeader) {
    int sequenceIndex = bamHeader.getSequenceIndex(interval.getContig());
    return new QueryInterval(sequenceIndex, interval.getStart(), interval.getStop());
  }
}
