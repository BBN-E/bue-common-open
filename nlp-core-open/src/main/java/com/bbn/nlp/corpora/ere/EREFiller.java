package com.bbn.nlp.corpora.ere;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;


public final class EREFiller implements ERESpanning {
  private final String id;
  private final String type;
  private final ERESpan extent;
  @Nullable
  private final String nomTime;

  private EREFiller(final String id, final String type, final ERESpan extent, @Nullable final String nomTime) {
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.extent = checkNotNull(extent);
    // nullable
    this.nomTime = nomTime;
  }

  public static EREFiller from(final String id, final String type, final ERESpan extent) {
    return new EREFiller(id, type, extent, null);
  }

  public static EREFiller fromTime(final String id, final String type, String normalizedTime,
      final ERESpan extent) {
    return new EREFiller(id, type, extent, normalizedTime);
  }

  @Override
  public String getID() {
    return id;
  }

  public String getType() {
    return type;
  }

  @Override
  public ERESpan getExtent() {
    return extent;
  }

  public Optional<String> getNormalizedTime() {
    return Optional.fromNullable(nomTime);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final EREFiller other = (EREFiller) obj;
    return Objects.equal(id, other.id);
  }

}


