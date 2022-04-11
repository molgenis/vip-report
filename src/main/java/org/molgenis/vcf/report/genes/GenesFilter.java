package org.molgenis.vcf.report.genes;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.genes.GeneFileParser.readGeneFile;

import htsjdk.variant.variantcontext.VariantContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;
import org.molgenis.vcf.report.utils.BestCompressionGZIPOutputStream;

public class GenesFilter {
  private static final String CHROM_PREFIX = "chr";

  private final VcfIntervalCalculator vcfIntervalCalculator;
  private final Path genesFile;

  public GenesFilter(VcfIntervalCalculator vcfIntervalCalculator, Path genesFile) {
    this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
    this.genesFile = requireNonNull(genesFile);
  }

  public byte[] filter(Iterable<VariantContext> variants, int flanking) {
    List<ContigInterval> contigIntervals = vcfIntervalCalculator.calculate(variants, flanking);
    List<GeneLine> geneLines = readGeneFile(genesFile);

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try (Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
      for (GeneLine geneLine : geneLines) {
        for (ContigInterval contigInterval : contigIntervals) {
          if (geneLine.getChrom().equals(contigInterval.getContig().startsWith(CHROM_PREFIX)? contigInterval.getContig() : CHROM_PREFIX + contigInterval.getContig())
              && ((geneLine.getTxStart() >= contigInterval.getStart()
                      && geneLine.getTxStart()
                          <= contigInterval.getStop()) // feature start in region
                  || (geneLine.getTxEnd() >= contigInterval.getStart()
                      && geneLine.getTxEnd() <= contigInterval.getStop()) // feature end in region
                  || (geneLine.getTxStart() <= contigInterval.getStart()
                      && geneLine.getTxEnd()
                          >= contigInterval.getStop()))) { // feature overlaps entire contig
            writer.write(geneLine.toGeneLineString());
            writer.write("\n");
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return BestCompressionGZIPOutputStream.compress(output.toByteArray());
  }
}
