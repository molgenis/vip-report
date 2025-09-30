package org.molgenis.vcf.report.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.molgenis.vcf.report.model.Items;
import org.molgenis.vcf.utils.sample.model.AffectedStatus;
import org.molgenis.vcf.utils.sample.model.Person;
import org.molgenis.vcf.utils.sample.model.Sample;
import org.molgenis.vcf.utils.sample.model.Sex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleRepositoryTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private SampleRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(pstmt);
        repo = new SampleRepository();
    }

    @Test
    void testInsertSamplesBatching() throws Exception {
        Sample sample1 = mock(Sample.class);
        Sample sample2 = mock(Sample.class);
        Person person1 = mock(Person.class);
        Person person2 = mock(Person.class);

        when(sample1.getPerson()).thenReturn(person1);
        when(sample2.getPerson()).thenReturn(person2);
        when(sample1.getIndex()).thenReturn(1);
        when(sample2.getIndex()).thenReturn(2);
        when(sample1.isProband()).thenReturn(true);
        when(sample2.isProband()).thenReturn(false);

        when(person1.getFamilyId()).thenReturn("fam1");
        when(person1.getIndividualId()).thenReturn("ind1");
        when(person1.getPaternalId()).thenReturn("dad1");
        when(person1.getMaternalId()).thenReturn("mom1");
        when(person1.getSex()).thenReturn(Sex.MALE);
        when(person1.getAffectedStatus()).thenReturn(AffectedStatus.AFFECTED);

        when(person2.getFamilyId()).thenReturn("fam2");
        when(person2.getIndividualId()).thenReturn("ind2");
        when(person2.getPaternalId()).thenReturn("dad2");
        when(person2.getMaternalId()).thenReturn("mom2");
        when(person2.getSex()).thenReturn(Sex.FEMALE);
        when(person2.getAffectedStatus()).thenReturn(AffectedStatus.UNAFFECTED);

        Items<Sample> sampleItems = mock(Items.class);
        when(sampleItems.getItems()).thenReturn(List.of(sample1, sample2));

        doNothing().when(pstmt).addBatch();
        doReturn(new int[]{1}).when(pstmt).executeBatch();

        repo.insertSamples(conn, sampleItems);

        verify(pstmt).setInt(1, 1);
        verify(pstmt).setString(2, "fam1");
        verify(pstmt).setString(3, "ind1");
        verify(pstmt).setString(4, "dad1");
        verify(pstmt).setString(5, "mom1");
        verify(pstmt).setString(6,"MALE" );
        verify(pstmt).setString(7, "AFFECTED");
        verify(pstmt).setInt(8, 1);
        verify(pstmt).setInt(9, 1);
        verify(pstmt).setInt(1, 2);
        verify(pstmt).setString(2, "fam2");
        verify(pstmt).setString(3, "ind2");
        verify(pstmt).setString(4, "dad2");
        verify(pstmt).setString(5, "mom2");
        verify(pstmt).setString(6, "FEMALE");
        verify(pstmt).setString(7, "UNAFFECTED");
        verify(pstmt).setInt(8, 2);
        verify(pstmt).setInt(9, 0);
        verify(pstmt, times(2)).addBatch();
        verify(pstmt, times(1)).executeBatch();
    }
}
