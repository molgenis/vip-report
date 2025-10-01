package org.molgenis.vcf.report.repository;

import htsjdk.variant.variantcontext.Allele;
import htsjdk.variant.variantcontext.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.vcf.utils.model.metadata.FieldMetadata;
import org.molgenis.vcf.utils.model.metadata.FieldMetadatas;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FormatRepositoryTest {

    private FormatRepository formatRepository;

    @BeforeEach
    void setUp() {
        formatRepository = new FormatRepository();
    }

    @Test
    void testInsertFormatData() throws SQLException {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(conn.createStatement()).thenReturn(ps);
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("field")).thenReturn("test");
        when(rs.getString("value")).thenReturn("value");
        when(rs.getInt("id")).thenReturn(1);
        when(rs.next()).thenReturn(true).thenReturn(false);
        when(ps.executeQuery("SELECT id, field, value FROM categories")).thenReturn(rs);

        VariantContext vc = mock(VariantContext.class);
        Genotype genotype1 = mock(Genotype.class);
        Genotype genotype2 = mock(Genotype.class);
        ArrayList<Genotype> genotypes = new ArrayList<>();
        genotypes.add(genotype1);
        genotypes.add(genotype2);

        GenotypesContext genotypesContext = GenotypesContext.create(genotypes);
        when(vc.getGenotypes()).thenReturn(genotypesContext);

        List<String> formatColumns = Arrays.asList("GT", "DP");
        FieldMetadatas fieldMetadatas = mock(FieldMetadatas.class);
        FieldMetadata fmGT = mock(FieldMetadata.class);
        FieldMetadata fmDP = mock(FieldMetadata.class);
        when(fieldMetadatas.getFormat()).thenReturn(Map.of("GT", fmGT, "DP", fmDP));

        Sample sample1 = mock(Sample.class);
        Sample sample2 = mock(Sample.class);
        Person person1 = mock(Person.class);
        Person person2 = mock(Person.class);
        when(person1.getIndividualId()).thenReturn("sampleA");
        when(person2.getIndividualId()).thenReturn("sampleB");
        when(sample1.getPerson()).thenReturn(person1);
        when(sample2.getPerson()).thenReturn(person2);
        when(sample1.getIndex()).thenReturn(1);
        when(sample2.getIndex()).thenReturn(2);
        List<Sample> samples = Arrays.asList(sample1, sample2);

        when(genotype1.getSampleName()).thenReturn("sampleA");
        when(genotype2.getSampleName()).thenReturn("sampleB");
        when(genotype1.getPloidy()).thenReturn(2);
        when(genotype2.getPloidy()).thenReturn(2);
        when(genotype1.isPhased()).thenReturn(false);
        when(genotype2.isPhased()).thenReturn(false);
        Allele alleleA = mock(Allele.class);
        Allele alleleB = mock(Allele.class);
        when(genotype1.getAllele(0)).thenReturn(alleleA);
        when(genotype1.getAllele(1)).thenReturn(alleleB);
        when(vc.getAlleleIndex(any(Allele.class))).thenReturn(0);

        when(genotype1.hasAnyAttribute(anyString())).thenReturn(true);
        when(genotype1.getAnyAttribute(anyString())).thenReturn("testValue");
        when(genotype2.hasAnyAttribute(anyString())).thenReturn(false);

        formatRepository.insertFormatData(conn, vc, formatColumns, 1, fieldMetadatas, samples);

        verify(ps).setInt(1, 1);
        verify(ps).setInt(2, 2);
        verify(ps).setInt(2, 1);
        verify(ps).setString(3, "0/0");
        verify(ps).setString(4, "[\"testValue\"]");
        verify(ps).setString(3, null);
        verify(ps).setString(4, null);
        verify(ps, times(2)).addBatch();
        verify(ps).executeBatch();
        verify(conn).prepareStatement(anyString());
    }
}
