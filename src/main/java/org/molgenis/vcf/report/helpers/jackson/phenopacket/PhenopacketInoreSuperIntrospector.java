package org.molgenis.vcf.report.helpers.jackson.phenopacket;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.google.protobuf.GeneratedMessageV3;

public class PhenopacketInoreSuperIntrospector extends JacksonAnnotationIntrospector {
    @Override
    public boolean hasIgnoreMarker(final AnnotatedMember m) {
      return m.getDeclaringClass() == GeneratedMessageV3.class || super.hasIgnoreMarker(m);
    }
}
