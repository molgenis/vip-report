package org.molgenis.vcf.report.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SqlUtils {
    public static List<String> extractCSQFields(String description) {
        int idx = description.indexOf("Format: ");
        if (idx == -1) return Collections.emptyList();
        String format = description.substring(idx + 8);
        return Arrays.asList(format.split("\\|"));
    }
}
