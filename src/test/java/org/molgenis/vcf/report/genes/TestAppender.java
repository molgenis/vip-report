package org.molgenis.vcf.report.genes;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.List;

public class TestAppender extends AppenderBase<ILoggingEvent> {

  static List<ILoggingEvent> events = new ArrayList<>();

  public TestAppender() {
    System.out.println("");
  }

  public static void reset() {
    events.clear();
  }

  @Override
  protected void append(ILoggingEvent e) {
    events.add(e);
  }
}
