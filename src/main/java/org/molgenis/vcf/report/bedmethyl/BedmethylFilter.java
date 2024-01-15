package org.molgenis.vcf.report.bedmethyl;


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
import org.molgenis.vcf.report.utils.BestCompressionGZIPOutputStream;

import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

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
        List<BedmethylLine> bedmethylLines = Collections.emptyList();


        if (bedmethylFile != null){
            bedmethylLines = readBedmethylFile(bedmethylFile);
        }

        try (
                StringWriter stringWriter = new StringWriter()
                ) {
            StatefulBeanToCsv<BedmethylLine> beanToCsv = new StatefulBeanToCsvBuilder<BedmethylLine>(stringWriter).withSeparator('\t').build();

                for (BedmethylLine bedmethylLine : bedmethylLines) {
                    boolean isAdded = false;
                    for (ContigInterval contigInterval : contigIntervals) {
                        if (!isAdded
                                && bedmethylLine.getContig().equals(contigInterval.getContig())
                                && isOverlappingFeature(bedmethylLine, contigInterval)) {

                            beanToCsv.write(bedmethylLine);
                            isAdded = true;
                        }
                    }
                }
            outputStream.write(stringWriter.toString().getBytes());

        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new  UncheckedIOException(e);
        }

        return outputStream.toByteArray();
    }
    private static List<BedmethylLine> readBedmethylFile(Path bedmethylFile) {
        List<BedmethylLine> bedmethylLines;
        try (InputStream fileStream = new FileInputStream(bedmethylFile.toFile());
             InputStream gzipStream = new GZIPInputStream(fileStream);
             Reader reader = new BufferedReader(new InputStreamReader(gzipStream))) {
            CsvToBean<BedmethylLine> csvToBean =
                    new CsvToBeanBuilder<BedmethylLine>(reader)
                            .withSeparator('\t')
                            .withType(BedmethylLine.class)
                            .withThrowExceptions(false)
                            .withIgnoreQuotations(true)
                            .build();
            bedmethylLines = csvToBean.parse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bedmethylLines;
    }
    private boolean isOverlappingFeature(BedmethylLine bedmethylLine, ContigInterval contigInterval) {
        return (bedmethylLine.getStart() >= contigInterval.getStart()
                && bedmethylLine.getStart() <= contigInterval.getStop()) // feature start in region
                || (bedmethylLine.getEnd() >= contigInterval.getStart()
                && bedmethylLine.getEnd() <= contigInterval.getStop()) // feature end in region
                || (bedmethylLine.getStart() <= contigInterval.getStart()
                && bedmethylLine.getEnd() >= contigInterval.getStop());
    }



}
