package org.molgenis.vcf.report.utils;

import htsjdk.tribble.readers.LineReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;

public class Utf8LineReader implements LineReader {
    private final BufferedReader br;
    public Utf8LineReader(BufferedReader br) { this.br = br; }
    @Override
    public String readLine() throws IOException { return br.readLine(); }
    @Override
    public void close() {
        try {
            br.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}