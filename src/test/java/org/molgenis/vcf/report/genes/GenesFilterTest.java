package org.molgenis.vcf.report.genes;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import htsjdk.variant.variantcontext.VariantContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.GZIPInputStream;

import htsjdk.variant.vcf.VCFHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VcfIntervalCalculator;

@ExtendWith(MockitoExtension.class)
class GenesFilterTest {

  private GenesFilter genesFilter;
  @Mock
  private VcfIntervalCalculator vcfIntervalCalculator;

  @BeforeEach
  void setUp() {
    Path genesGzPath = Path.of("src", "test", "resources", "example.genes.gff.gz");
    genesFilter = new GenesFilter(vcfIntervalCalculator, genesGzPath);
  }

  @Test
  void filter() {
    VariantContext variantContext = mock(VariantContext.class);
    Iterable<VariantContext> variants = singletonList(variantContext);
    ContigInterval contigInterval = mock(ContigInterval.class);
    when(contigInterval.getContig()).thenReturn("chr1");
    when(contigInterval.getStart()).thenReturn(11895);
    when(contigInterval.getStop()).thenReturn(11900);
    ContigInterval contigInterval2 = mock(ContigInterval.class);
    when(contigInterval2.getContig()).thenReturn("chr1");
    when(contigInterval2.getStart()).thenReturn(11870);
    when(contigInterval2.getStop()).thenReturn(14400);
    ContigInterval contigInterval3 = mock(ContigInterval.class);
    when(contigInterval3.getContig()).thenReturn("chr1");
    when(contigInterval3.getStart()).thenReturn(11890);
    when(contigInterval3.getStop()).thenReturn(14410);
    ContigInterval contigInterval4 = mock(ContigInterval.class);
    when(contigInterval4.getContig()).thenReturn("chr1");
    when(contigInterval4.getStart()).thenReturn(11890);
    when(contigInterval4.getStop()).thenReturn(14408);
    VCFHeader vcfHeader = mock(VCFHeader.class);
    when(vcfIntervalCalculator.calculate(vcfHeader, variants, 250)).thenReturn(
        List.of(contigInterval, contigInterval2, contigInterval3, contigInterval4));

    assertEquals(
        "##gff-version 3.1.25\n"
            + "chr1\tBestRefSeq\tpseudogene\t11874\t14409\t.\t+\t.\tID=gene-DDX11L1;Dbxref=GeneID%3A100287102,HGNC%3AHGNC%3A37102;Name=DDX11L1;description=DEAD%2FH-box helicase 11 like 1 %28pseudogene%29;gbkey=Gene;gene=DDX11L1;gene_biotype=transcribed_pseudogene;pseudo=true\n"
            + "chr1\tBestRefSeq\ttranscript\t11874\t14409\t.\t+\t.\tID=rna-NR_046018.2;Parent=gene-DDX11L1;Dbxref=GeneID%3A100287102,Genbank%3ANR_046018.2,HGNC%3AHGNC%3A37102;Name=NR_046018.2;gbkey=misc_RNA;gene=DDX11L1;product=DEAD%2FH-box helicase 11 like 1 %28pseudogene%29;pseudo=true;transcript_id=NR_046018.2\n"
            + "chr1\tBestRefSeq\texon\t11874\t12227\t.\t+\t.\tID=exon-NR_046018.2-1;Parent=rna-NR_046018.2;Dbxref=GeneID%3A100287102,Genbank%3ANR_046018.2,HGNC%3AHGNC%3A37102;gbkey=misc_RNA;gene=DDX11L1;product=DEAD%2FH-box helicase 11 like 1 %28pseudogene%29;pseudo=true;transcript_id=NR_046018.2\n",
        decompress(genesFilter.filter(vcfHeader, variants, 250)));
  }

  private static String decompress(byte[] bytes) {
    try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
      return new String(gzipInputStream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}