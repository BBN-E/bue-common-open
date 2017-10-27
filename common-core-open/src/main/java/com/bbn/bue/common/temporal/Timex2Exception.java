package com.bbn.bue.common.temporal;

public final class Timex2Exception extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public Timex2Exception(final String msg) {
    super(msg);
  }

  public Timex2Exception(final String msg, final Exception e) {
    super(msg, e);
  }

  public static Timex2Exception cannotParse(final String unparseable, final Exception e) {
    return new Timex2Exception(
        String.format("Cannot parse timex expression or part of expression %s", unparseable),
        e);
  }
}
