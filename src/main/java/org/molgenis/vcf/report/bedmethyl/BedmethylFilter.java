package org.molgenis.vcf.report.bedmethyl;


import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import htsjdk.variant.vcf.VCFFileReader;
import org.molgenis.vcf.report.fasta.ContigInterval;
import org.molgenis.vcf.report.fasta.VariantIntervalCalculator;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.molgenis.vcf.report.model.BedmethylLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import static java.util.Objects.requireNonNull;

public class BedmethylFilter {

    private final VariantIntervalCalculator variantIntervalCalculator;

    private final Path bedmethylFile;

    public BedmethylFilter(VariantIntervalCalculator variantIntervalCalculator, Path bedmethylFile) {
        this.variantIntervalCalculator = requireNonNull(variantIntervalCalculator);
        this.bedmethylFile = requireNonNull(bedmethylFile);

    }

    public byte[] filter(VCFFileReader variants, Map<String, SampleSettings.CramPath> cramPaths, Path reference) {
        List<ContigInterval> contigIntervals = variantIntervalCalculator.calculate(variants, cramPaths, reference);
        return filter(contigIntervals);
    }

    private byte[] filter(List<ContigInterval> contigIntervals) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (
                InputStream fileStream = new FileInputStream(bedmethylFile.toFile());
                Reader reader = new BufferedReader(new InputStreamReader(fileStream));
                OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                CSVWriter csvWriter = new CSVWriter(streamWriter,
                        '\t', ICSVWriter.NO_QUOTE_CHARACTER,
                        ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        ICSVWriter.DEFAULT_LINE_END)
        ) {
            StatefulBeanToCsv<BedmethylLine> beanToCsv = new StatefulBeanToCsvBuilder<BedmethylLine>(csvWriter)
                    .build();

            CsvToBean<BedmethylLine> csvToBean =
                    new CsvToBeanBuilder<BedmethylLine>(reader)
                            .withSeparator('\t')
                            .withType(BedmethylLine.class)
                            .withThrowExceptions(false)
                            .withIgnoreQuotations(true)
                            .build();
            beanToCsv.write(csvToBean.stream().filter(bedmethylLine -> isFeature(contigIntervals, bedmethylLine)));
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return outputStream.toByteArray();
    }

    private static boolean isFeature(List<ContigInterval> contigIntervals, BedmethylLine bedmethylLine) {
        for (ContigInterval contigInterval : contigIntervals) {
            if (bedmethylLine.getContig().equals(contigInterval.getContig())
                    && isOverlappingFeature(bedmethylLine, contigInterval)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOverlappingFeature(BedmethylLine bedmethylLine, ContigInterval contigInterval) {
        return (bedmethylLine.getStart() >= contigInterval.getStart()
                && bedmethylLine.getStart() <= contigInterval.getStop()) // feature start in region
                || (bedmethylLine.getEnd() >= contigInterval.getStart()
                && bedmethylLine.getEnd() <= contigInterval.getStop()) // feature end in region
                || (bedmethylLine.getStart() <= contigInterval.getStart()
                && bedmethylLine.getEnd() >= contigInterval.getStop());
    }

}
