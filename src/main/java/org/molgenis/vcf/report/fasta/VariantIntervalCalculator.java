package org.molgenis.vcf.report.fasta;

import htsjdk.variant.vcf.VCFIterator;
import org.molgenis.vcf.report.generator.SampleSettings;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.molgenis.vcf.report.utils.IntervalUtils.mergeIntervals;

@Component
public class VariantIntervalCalculator {
    public static final int FLANKING = 10000;
    private final VcfIntervalCalculator vcfIntervalCalculator;
    private final CramIntervalCalculator cramIntervalCalculator;

    public VariantIntervalCalculator(VcfIntervalCalculator vcfIntervalCalculator, CramIntervalCalculator cramIntervalCalculator) {
        this.vcfIntervalCalculator = requireNonNull(vcfIntervalCalculator);
        this.cramIntervalCalculator = requireNonNull(cramIntervalCalculator);
    }

    public List<ContigInterval> calculate(VCFIterator vcfIterator, Map<String, SampleSettings.CramPath> cramPaths, Path referencePath) {
        Map<String, List<ContigInterval>> cramIntervals;
        List<ContigInterval> intervals = new ArrayList<>();
        if(cramPaths != null && !cramPaths.isEmpty()) {
            Map<String, List<ContigInterval>> vcfIntervals = vcfIntervalCalculator.calculate(vcfIterator.getHeader(), vcfIterator, FLANKING, null);
            cramIntervals = cramIntervalCalculator.calculate(cramPaths, referencePath);
            intervals = mergeIntervalLists(cramIntervals, vcfIntervals);
        }else{
            Map<String, List<ContigInterval>> intervalMap =
                    vcfIntervalCalculator.calculate(vcfIterator.getHeader(), vcfIterator, FLANKING, null);
            for(Map.Entry<String, List<ContigInterval>> entry : intervalMap.entrySet()){
                intervals.addAll(mergeIntervals(entry.getValue()));
            }
        }
        return intervals;
    }

    private List<ContigInterval> mergeIntervalLists(Map<String, List<ContigInterval>> cramIntervalMap, Map<String, List<ContigInterval>> vcfIntervalMap) {
        List<ContigInterval> intervals = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        keys.addAll(cramIntervalMap.keySet());
        keys.addAll(vcfIntervalMap.keySet());
        for(String key : keys){
            List<ContigInterval> vcfIntervals = vcfIntervalMap.containsKey(key) ? vcfIntervalMap.get(key) : Collections.emptyList();
            List<ContigInterval> cramIntervals = cramIntervalMap.containsKey(key) ? cramIntervalMap.get(key) : Collections.emptyList();
            List<ContigInterval> combinedIntervals = new ArrayList<>();
            combinedIntervals.addAll(vcfIntervals);
            combinedIntervals.addAll(cramIntervals);
            intervals.addAll(mergeIntervals(combinedIntervals));
        }
        return intervals;
    }

}
