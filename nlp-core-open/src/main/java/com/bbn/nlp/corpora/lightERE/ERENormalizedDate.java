package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public final class ERENormalizedDate {

  private final String date;

  private ERENormalizedDate(final String date) {
    this.date = date;
  }

  public String getDate() {
    return date;
  }

  @Override
  public String toString() {
    return "ERENormalizedDate{" +
        "date='" + date + '\'' +
        '}';
  }

  public static ERENormalizedDate from(final String date) {
    return new ERENormalizedDate(date);
  }
}

