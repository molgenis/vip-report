package org.molgenis.vcf.report.fasta;

import static java.util.Objects.requireNonNull;

import htsjdk.samtools.reference.FastaReferenceWriter;
import htsjdk.samtools.reference.FastaReferenceWriterBuilder;
import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import org.molgenis.vcf.report.utils.BestCompressionGZIPOutputStream;

public class FastaSlicer {

  private final ReferenceSequenceFile referenceSequenceFile;

  public FastaSlicer(ReferenceSequenceFile referenceSequenceFile) {
    this.referenceSequenceFile = requireNonNull(referenceSequenceFile);
  }

  public FastaSlice slice(ContigInterval interval) {
    byte[] fasta = createFasta(interval);
    byte[] fastaGz = BestCompressionGZIPOutputStream.compress(fasta);
    return new FastaSlice(interval, fastaGz);
  }

  private byte[] createFasta(ContigInterval interval) {
    ReferenceSequence referenceSequence =
        referenceSequenceFile.getSubsequenceAt(
            interval.getContig(), interval.getStart(), interval.getStop());

    String referenceSequenceName =
        interval.getContig() + ':' + interval.getStart() + '-' + interval.getStop();
    ReferenceSequence renamedReferenceSequence =
        new ReferenceSequence(
            referenceSequenceName,
            referenceSequence.getContigIndex(),
            referenceSequence.getBases());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (FastaReferenceWriter fastaReferenceWriter = createFastaReferenceWriter(outputStream)) {
      fastaReferenceWriter.addSequence(renamedReferenceSequence);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return outputStream.toByteArray();
  }

  private static FastaReferenceWriter createFastaReferenceWriter(OutputStream outputStream)
      throws IOException {
    return new FastaReferenceWriterBuilder()
        .setFastaOutput(outputStream)
        .setBasesPerLine(Integer.MAX_VALUE)
        .setEmitMd5(false)
        .setMakeFaiOutput(false)
        .setMakeDictOutput(false)
        .setMakeGziOutput(false)
        .build();
  }
}
