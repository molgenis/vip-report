package org.molgenis.vcf.report.genes;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * bin	585	smallint(5) unsigned	range	Indexing field to speed chromosome range queries.
 * name	NR_046018.2	varchar(255)	values	Name of gene (usually transcript_id from GTF)
 * chrom	chr1	varchar(255)	values	Reference sequence chromosome or scaffold
 * strand	+	char(1)	values	+ or - for strand
 * txStart	11873	int(10) unsigned	range	Transcription start position (or end position for minus strand item)
 * txEnd	14409	int(10) unsigned	range	Transcription end position (or start position for minus strand item)
 * cdsStart	14409	int(10) unsigned	range	Coding region start (or end position for minus strand item)
 * cdsEnd	14409	int(10) unsigned	range	Coding region end (or start position for minus strand item)
 * exonCount	3	int(10) unsigned	range	Number of exons
 * exonStarts	11873,12612,13220,	longblob	 	Exon start positions (or end positions for minus strand item)
 * exonEnds	12227,12721,14409,	longblob	 	Exon end positions (or start positions for minus strand item)
 * score	0	int(11)	range	score
 * name2	DDX11L1	varchar(255)	values	Alternate name (e.g. gene_id from GTF)
 * cdsStartStat	none	enum('none', 'unk', 'incmpl', 'cmpl')	values	Status of CDS start annotation (none, unknown, incomplete, or complete)
 * cdsEndStat	none	enum('none', 'unk', 'incmpl', 'cmpl')	values	Status of CDS end annotation (none, unknown, incomplete, or complete)
 * exonFrames	-1,-1,-1,	longblob	 	Exon frame {0,1,2}, or -1 if no frame for exon
 * */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneLine {
  private static final String FORMAT = "%d\t%s\t%s\t%s\t%d\t%d\t%d\t%d\t%d\t%s\t%s\t%d\t%s\t%s\t%s\t%s";

  @CsvBindByPosition(position = 0, required = true)
  int bin;

  @CsvBindByPosition(position = 1, required = true)
  String name;

  @CsvBindByPosition(position = 2, required = true)
  String chrom;

  @CsvBindByPosition(position = 3, required = true)
  char strand;

  @CsvBindByPosition(position = 4, required = true)
  int txStart;

  @CsvBindByPosition(position = 5, required = true)
  int txEnd;

  @CsvBindByPosition(position = 6, required = true)
  int cdsStart;

  @CsvBindByPosition(position = 7, required = true)
  int cdsEnd;

  @CsvBindByPosition(position = 8, required = true)
  int exonCount;

  @CsvBindByPosition(position = 9, required = true)
  String exonStarts;

  @CsvBindByPosition(position = 10, required = true)
  String exonEnds;

  @CsvBindByPosition(position = 11, required = true)
  int score;

  @CsvBindByPosition(position = 12, required = true)
  String name2;

  @CsvBindByPosition(position = 13, required = true)
  String cdsStartStat;

  @CsvBindByPosition(position = 14, required = true)
  String cdsEndStat;

  @CsvBindByPosition(position = 15, required = true)
  String exonFrames;

  public String toGeneLineString() {
    return String.format(
        FORMAT,
        bin,
        name,
        chrom,
        strand,
        txStart,
        txEnd,
        cdsStart,
        cdsEnd,
        exonCount,
        exonStarts,
        exonEnds,
        score,
        name2,
        cdsStartStat,
        cdsEndStat,
        exonFrames);
  }
}
