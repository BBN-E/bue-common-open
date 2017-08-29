package com.bbn.bue.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ibm.icu.util.ULocale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a wrapper for ICU Locale which handles serialization
 *
 * @author Ryan Gabbard
 */
public final class SerifLocale {

  private final ULocale icuLocale;

  private SerifLocale(final ULocale icuLocale) {
    this.icuLocale = checkNotNull(icuLocale);
  }

  @JsonCreator
  public static SerifLocale forLocaleString(@JsonProperty("localeString") String localeString) {
    return new SerifLocale(new ULocale(localeString));
  }

  public static SerifLocale forIcuLocale(ULocale icuLocale) {
    return new SerifLocale(icuLocale);
  }

  @JsonProperty("localeString")
  protected String localeString() {
    return icuLocale.getName();
  }

  public ULocale asIcuLocale() {
    return icuLocale;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final SerifLocale that = (SerifLocale) o;

    return icuLocale != null ? icuLocale.equals(that.icuLocale) : that.icuLocale == null;

  }

  @Override
  public int hashCode() {
    return icuLocale != null ? icuLocale.hashCode() : 0;
  }

  @Override
  public String toString() {
    return localeString();
  }
}
