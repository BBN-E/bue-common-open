package com.bbn.nlp.corpora.ere;

import com.google.common.base.Objects;
import com.google.common.base.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

public final class EREEntityMention implements ERESpanning {
  private final String id;
  private final String type;
  private final ERESpan extent;
  private final Optional<ERESpan> head;

  private EREEntityMention(final String id, final String type, final ERESpan extent, final Optional<ERESpan> head) {
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.extent = checkNotNull(extent);
    this.head = head;
  }

  public static EREEntityMention from(final String id, final String type, final ERESpan extent, final Optional<ERESpan> head) {
    return new EREEntityMention(id, type, extent, head);
  }

  public String getType() {
    return type;
  }

  @Override
  public String getID() {
    return id;
  }

  @Override
  public ERESpan getExtent() {
    return extent;
  }

  public Optional<ERESpan> getHead() {
    return head;
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
    final EREEntityMention other = (EREEntityMention) obj;
    return Objects.equal(id, other.id);
  }


  @Override
  public String toString() {
    String s = extent + "/" + id + "/" + type;
    if (head.isPresent()) {
      s += "/head=" + head;
    }
    return s;
  }
}
