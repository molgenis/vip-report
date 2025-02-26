package org.molgenis.vcf.report.fasta;

import htsjdk.samtools.CRAMFileReader;

import java.nio.file.Path;
import java.util.*;

import htsjdk.samtools.SAMRecordIterator;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class CramIntervalCalculator {
    private final CramReaderFactory cramReaderFactory;

    public CramIntervalCalculator(CramReaderFactory cramReaderFactory) {
        this.cramReaderFactory = requireNonNull(cramReaderFactory);
    }

    public Map<String, List<ContigInterval>> calculate(Map<String, SampleSettings.CramPath> crampaths, Path reference) {
        Map<String, List<ContigInterval>> intervalMap = new LinkedHashMap<>();
        for (SampleSettings.CramPath cramPath : crampaths.values()) {
            try (CRAMFileReader reader = cramReaderFactory.create(cramPath, reference)) {
                SAMRecordIterator iterator = reader.getIterator();
                iterator.stream().forEach(cramRecord -> {
                    //skip the unmapped reads
                    if (cramRecord.getContig() != null) {
                        ContigInterval contigInterval = new ContigInterval(cramRecord.getContig(), cramRecord.getStart(), cramRecord.getEnd());
                        intervalMap.computeIfAbsent(cramRecord.getContig(), k -> new ArrayList<>()).add(contigInterval);
                    }
                });
            }

        }
        return intervalMap;
    }
}
