package org.molgenis.vcf.report.fasta;

import htsjdk.samtools.CRAMFileReader;

import java.nio.file.Path;
import java.util.*;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.utils.IntervalUtils.mergeIntervals;

@Component
public class CramIntervalCalculator {
    private final CramReaderFactory cramReaderFactory;

    public CramIntervalCalculator(CramReaderFactory cramReaderFactory) {
        this.cramReaderFactory = requireNonNull(cramReaderFactory);
    }

    public List<ContigInterval> calculate(
            Map<String, SampleSettings.CramPath> crampaths, Path reference) {
        Map<String, List<ContigInterval>> intervalMap =
                computeIntervalMap(crampaths, reference);
        List<ContigInterval> intervals = new ArrayList<>();
        intervalMap.forEach((key, value) -> intervals.addAll(mergeIntervals(value)));
        return intervals;
    }

    private Map<String, List<ContigInterval>> computeIntervalMap(Map<String, SampleSettings.CramPath> crampaths, Path reference) {
        Map<String, List<ContigInterval>> intervalMap = new LinkedHashMap<>();
        for (SampleSettings.CramPath cramPath : crampaths.values()) {
            try (CRAMFileReader reader = cramReaderFactory.create(cramPath, reference)) {
                SAMRecordIterator iterator = reader.getIterator();
                iterator.stream().forEach(cramRecord -> {
                    ContigInterval contigInterval = new ContigInterval(cramRecord.getContig(), cramRecord.getStart(), cramRecord.getEnd());
                    intervalMap.computeIfAbsent(cramRecord.getContig(), k -> new ArrayList<>()).add(contigInterval);
                });
            }

        }
        return intervalMap;
    }
}
