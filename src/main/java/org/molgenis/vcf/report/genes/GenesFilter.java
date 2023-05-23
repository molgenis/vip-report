package org.molgenis.vcf.report.genes;

import static java.util.Objects.requireNonNull;

import htsjdk.tribble.AbstractFeatureReader;
import htsjdk.tribble.gff.Gff3Codec;
import htsjdk.tribble.gff.Gff3Codec.DecodeDepth;
import htsjdk.tribble.gff.Gff3Feature;
import htsjdk.tribble.gff.Gff3Writer;
import htsjdk.tribble.readers.LineIterator;
import htsjdk.variant.variantcontext.VariantContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

import htsjdk.variant.vcf.VCFHeader;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;
import org.molgenis.vcf.report.utils.BestCompressionGZIPOutputStream;

public class GenesFilter {
  private static final List<String> FEATURE_SOURCES = List.of("BestRefSeq", "Curated Genomic");
  private static final List<String> FEATURE_TYPES =
      List.of("transcript", "primary_transcript", "exon", "mRNA", "pseudogene", "gene");

  private final VcfIntervalCalculator vcfIntervalCalculator;
  private final Path genesFile;

  public GenesFilter(VcfIntervalCalculator vcfIntervalCalculator, Path genesFile) {
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
    this.genesFile = requireNonNull(genesFile);
  }

  public byte[] filter(VCFHeader vcfHeader, Iterable<VariantContext> variants, int flanking) {
    List<ContigInterval> contigIntervals = vcfIntervalCalculator.calculate(vcfHeader, variants, flanking);

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    final Gff3Codec codec = new Gff3Codec(DecodeDepth.SHALLOW);
    try (final AbstractFeatureReader<Gff3Feature, LineIterator> reader =
            AbstractFeatureReader.getFeatureReader(
                genesFile.toAbsolutePath().toString(), null, codec, false);
        Gff3Writer writer = new Gff3Writer(output)) {
      for (final Gff3Feature feature : reader.iterator()) {
        boolean isAdded = false;
        for (ContigInterval contigInterval : contigIntervals) {
          if (!isAdded
              && feature.getContig().equals(contigInterval.getContig())
              && isOverlappingFeature(feature, contigInterval)
              && FEATURE_TYPES.contains(feature.getType())
              && FEATURE_SOURCES.contains(feature.getSource())) {
            writer.addFeature(feature);
            isAdded = true;
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return BestCompressionGZIPOutputStream.compress(output.toByteArray());
  }

  private boolean isOverlappingFeature(Gff3Feature feature, ContigInterval contigInterval) {
    return (feature.getStart() >= contigInterval.getStart()
            && feature.getStart() <= contigInterval.getStop()) // feature start in region
        || (feature.getEnd() >= contigInterval.getStart()
            && feature.getEnd() <= contigInterval.getStop()) // feature end in region
        || (feature.getStart() <= contigInterval.getStart()
            && feature.getEnd() >= contigInterval.getStop());
  }
}
