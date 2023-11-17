package org.molgenis.vcf.report.utils;

import org.molgenis.vcf.report.fasta.ContigInterval;

import java.util.ArrayList;
import java.util.List;

public class IntervalUtils {

    private IntervalUtils(){}
    public static List<ContigInterval> mergeIntervals(List<ContigInterval> intervals) {
        List<ContigInterval> mergedIntervals = new ArrayList<>();
        if (intervals.size() < 2) {
            mergedIntervals = intervals;
        } else {
            ContigInterval interval = intervals.get(0);
            for (int i = 1; i < intervals.size(); ++i) {
                ContigInterval nextInterval = intervals.get(i);
                if (nextInterval.getStart() <= interval.getStop() + 1) {
                    //when intervals are based on crams the length can differ and although the start is greater
                    //the stop position can be lesser than the previous interval
                    int stop = interval.getStop() < nextInterval.getStop() ? nextInterval.getStop() : interval.getStop();
                    interval =
                            new ContigInterval(interval.getContig(), interval.getStart(), stop);
                } else {
                    mergedIntervals.add(interval);
                    interval = nextInterval;
                }
            }
            mergedIntervals.add(interval);
        }
        return mergedIntervals;
    }
}
