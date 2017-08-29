package com.bbn.nlp.corpora.lightERE;

/**
 * @author Jay DeYoung
 */
public final class EREDate {

  private final EREDateExtent dateExtent;
  private final ERENormalizedDate normalizedDate;

  private EREDate(final EREDateExtent dateExtent, final ERENormalizedDate normalizedDate) {
    this.dateExtent = dateExtent;
    this.normalizedDate = normalizedDate;
  }

  public EREDateExtent getDateExtent() {
    return dateExtent;
  }

  public ERENormalizedDate getNormalizedDate() {
    return normalizedDate;
  }

  @Override
  public String toString() {
    return "EREDate{" +
        "dateExtent=" + dateExtent +
        ", normalizedDate=" + normalizedDate +
        '}';
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private EREDateExtent dateExtent;
    private ERENormalizedDate normalizedDate;

    public Builder setDateExtent(final EREDateExtent dateExtent) {
      this.dateExtent = dateExtent;
      return this;
    }

    public Builder setNormalizedDate(final ERENormalizedDate normalizedDate) {
      this.normalizedDate = normalizedDate;
      return this;
    }

    public EREDate build() {
      return new EREDate(this.dateExtent, this.normalizedDate);
    }
  }
}

