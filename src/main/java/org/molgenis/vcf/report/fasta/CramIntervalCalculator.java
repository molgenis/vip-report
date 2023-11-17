package org.molgenis.vcf.report.fasta;

import htsjdk.samtools.CRAMFileReader;

import java.nio.file.Path;
import java.util.*;

import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.cram.ref.ReferenceSource;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.springframework.stereotype.Component;

import static org.molgenis.vcf.report.utils.IntervalUtils.mergeIntervals;

@Component
public class CramIntervalCalculator {

    public List<ContigInterval> calculate(
            Map<String, SampleSettings.CramPath> crampaths, Path reference) {
        Map<String, List<ContigInterval>> intervalMap =
                computeIntervalMap(crampaths, reference);
        List<ContigInterval> intervals = new ArrayList<>();
        intervalMap.forEach((key, value) -> intervals.addAll(mergeIntervals(value)));
        return intervals;
    }

    private static Map<String, List<ContigInterval>> computeIntervalMap(Map<String, SampleSettings.CramPath> crampaths, Path reference) {
        Map<String, List<ContigInterval>> intervalMap = new LinkedHashMap<>();
        for (SampleSettings.CramPath cramPath : crampaths.values()) {
            try (CRAMFileReader reader = new CRAMFileReader(cramPath.getCram().toFile(), cramPath.getCrai().toFile(), new ReferenceSource(reference))) {
                SAMRecordIterator it = reader.getIterator();
                while (it.hasNext()) {
                    SAMRecord record = it.next();
                    ContigInterval contigInterval = new ContigInterval(record.getContig(), record.getStart(), record.getEnd());
                    intervalMap.computeIfAbsent(record.getContig(), k -> new ArrayList<>()).add(contigInterval);
                }
            }
        }
        return intervalMap;
    }
}
