package org.molgenis.vcf.report.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SqlUtils {
    private SqlUtils(){}

    public static List<String> extractNestedFields(String description) {
        int idx = description.indexOf("Format: ");
        if (idx == -1) return Collections.emptyList();
        String format = description.substring(idx + 8);
        return Arrays.asList(format.split("\\|"));
    }
}
