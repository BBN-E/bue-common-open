package com.bbn.nlp.corpora.ere;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;


public final class EREFiller implements ERESpanning {
  private final String id;
  private final String type;
  private final ERESpan extent;

  private EREFiller(final String id, final String type, final ERESpan extent) {
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.extent = checkNotNull(extent);
  }
  
  public static EREFiller from(final String id, final String type, final ERESpan extent) {
    return new EREFiller(id, type, extent);
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


