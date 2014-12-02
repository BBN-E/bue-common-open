package com.bbn.bue.common.io;

import com.google.common.io.CharSink;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public final class StringSink extends CharSink {
  private StringWriter sw = new StringWriter();

  private StringSink() {
  }

  public static StringSink createEmpty() {
    return new StringSink();
  }

  @Override
  public Writer openStream() throws IOException {
    sw = new StringWriter();
    return sw;
  }

  public String getString() {
    return sw.toString();
  }
}
