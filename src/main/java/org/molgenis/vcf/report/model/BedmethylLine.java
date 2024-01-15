package org.molgenis.vcf.report.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BedmethylLine {
    @CsvBindByPosition(
            position = 0,
            required = true) String contig;
    @CsvBindByPosition(
            position = 1,
            required = true) Integer start;

    @CsvBindByPosition(
            position = 2,
            required = true) Integer end;

    @CsvBindByPosition(
            position = 3,
            required = true) String code;

    @CsvBindByPosition(
            position = 4,
            required = true) String score;

    @CsvBindByPosition(
            position = 5,
            required = true) String strand;

    @CsvBindByPosition(
            position = 6,
            required = true) Integer start2;

    @CsvBindByPosition(
            position = 7,
            required = true) Integer end2;

    @CsvBindByPosition(
            position = 8,
            required = true) String color;
    @CsvBindByPosition(
            position = 9,
            required = true) String nValidCov;

    @CsvBindByPosition(
            position = 10,
            required = true) String freq;

    @CsvBindByPosition(
            position = 11,
            required = true) String nMod;

    @CsvBindByPosition(
            position = 12,
            required = true) String nCanonical;

    @CsvBindByPosition(
            position = 13,
            required = true) String nOtherMod;

    @CsvBindByPosition(
            position = 14,
            required = true) String nDelete;

    @CsvBindByPosition(
            position = 15,
            required = true) String nFail;

    @CsvBindByPosition(
            position = 16,
            required = true) String nDiff;

    @CsvBindByPosition(
            position = 17,
            required = true) String nNoCall;



}
