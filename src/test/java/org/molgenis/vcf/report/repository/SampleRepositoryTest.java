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
import static org.molgenis.vcf.report.repository.SampleRepository.INSERT_SAMPLE_SQL;

@ExtendWith(MockitoExtension.class)
class SampleRepositoryTest {

    private Connection conn;
    private PreparedStatement pstmt;
    private PreparedStatement sexPstmt;
    private PreparedStatement affectedpstmt;
    private SampleRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        conn = mock(Connection.class);
        pstmt = mock(PreparedStatement.class);
        sexPstmt = mock(PreparedStatement.class);
        affectedpstmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(INSERT_SAMPLE_SQL)).thenReturn(pstmt);
        when(conn.prepareStatement("INSERT INTO sex (id, value) VALUES (?, ?)")).thenReturn(sexPstmt);
        when(conn.prepareStatement("INSERT INTO affectedStatus (id, value) VALUES (?, ?)")).thenReturn(affectedpstmt);
        repo = new SampleRepository();
    }

    @Test
    void testInsertSamplesBatching() throws Exception {
        Sample sample1 = mock(Sample.class);
        Sample dad1 = mock(Sample.class);
        Sample mom1 = mock(Sample.class);
        Sample sample2 = mock(Sample.class);
        Person person1 = mock(Person.class);
        Person person2 = mock(Person.class);
        Person person3 = mock(Person.class);
        Person person4 = mock(Person.class);

        when(sample1.getPerson()).thenReturn(person1);
        when(dad1.getPerson()).thenReturn(person3);
        when(mom1.getPerson()).thenReturn(person4);
        when(sample2.getPerson()).thenReturn(person2);
        when(sample1.getIndex()).thenReturn(1);
        when(dad1.getIndex()).thenReturn(3);
        when(mom1.getIndex()).thenReturn(4);
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
        when(person2.getPaternalId()).thenReturn("0");
        when(person2.getMaternalId()).thenReturn("0");
        when(person2.getSex()).thenReturn(Sex.FEMALE);
        when(person2.getAffectedStatus()).thenReturn(AffectedStatus.UNAFFECTED);

        when(person3.getFamilyId()).thenReturn("fam1");
        when(person3.getIndividualId()).thenReturn("dad1");
        when(person3.getPaternalId()).thenReturn("0");
        when(person3.getMaternalId()).thenReturn("0");
        when(person3.getSex()).thenReturn(Sex.MALE);
        when(person3.getAffectedStatus()).thenReturn(AffectedStatus.UNAFFECTED);

        when(person4.getFamilyId()).thenReturn("fam1");
        when(person4.getIndividualId()).thenReturn("mom1");
        when(person4.getPaternalId()).thenReturn("0");
        when(person4.getMaternalId()).thenReturn("0");
        when(person4.getSex()).thenReturn(Sex.FEMALE);
        when(person4.getAffectedStatus()).thenReturn(AffectedStatus.UNAFFECTED);

        Items<Sample> sampleItems = mock(Items.class);
        when(sampleItems.getItems()).thenReturn(List.of(sample1, sample2, dad1, mom1));

        doNothing().when(pstmt).addBatch();
        doReturn(new int[]{1}).when(pstmt).executeBatch();

        repo.insertSamples(conn, sampleItems);

        verify(pstmt).setInt(1, 1);
        verify(pstmt).setInt(1, 2);
        verify(pstmt).setInt(1, 3);
        verify(pstmt).setInt(1, 4);
        verify(pstmt, times(3)).setString(2, "fam1");
        verify(pstmt).setString(2, "fam2");
        verify(pstmt).setString(3, "ind1");
        verify(pstmt).setString(3, "dad1");
        verify(pstmt).setString(3, "mom1");
        verify(pstmt).setString(3, "ind2");
        verify(pstmt).setInt(4, 3);
        verify(pstmt).setInt(5, 4);
        verify(pstmt, times(2)).setInt(6, 0); //2 MALE
        verify(pstmt, times(2)).setInt(6, 1); //2 FEMALE
        verify(pstmt, times(1)).setInt(7, 0); //1 AFFECTED
        verify(pstmt, times(3)).setInt(7, 1); //3 UNAFFECTED
        verify(pstmt).setInt(8, 1);
        verify(pstmt).setInt(1, 2);
        verify(pstmt).setInt(1, 3);

        verify(pstmt, times(4)).addBatch();
        verify(pstmt, times(1)).executeBatch();
    }
}
