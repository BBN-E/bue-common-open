package com.bbn.nlp.events.scoring;

import com.bbn.bue.common.HasDocID;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.nlp.events.HasEventType;
import com.bbn.nlp.events.HasEventArgType;

import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public final class DocLevelEventArg implements HasDocID, HasEventType, HasEventArgType {

  private final Symbol docID;
  private final Symbol eventType;
  private final Symbol argumentType;
  private final String corefID;

  DocLevelEventArg(final Symbol docID, final Symbol eventType,
      final Symbol argumentType, final String corefID) {
    this.docID = checkNotNull(docID);
    this.eventType = checkNotNull(eventType);
    this.argumentType = checkNotNull(argumentType);
    this.corefID = checkNotNull(corefID);
  }

  public static DocLevelEventArg create(final Symbol docID, final Symbol eventType,
      final Symbol argumentType, final String corefID) {
    return new DocLevelEventArg(docID, eventType, argumentType, corefID);
  }

  @Override
  public Symbol docID() {
    return docID;
  }

  @Override
  public Symbol eventArgumentType() {
    return argumentType;
  }

  @Override
  public Symbol eventType() {
    return eventType;
  }

  public String corefID() {
    return corefID;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(docID, eventType, argumentType, corefID);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final DocLevelEventArg other = (DocLevelEventArg) obj;
    return Objects.equal(this.docID, other.docID)
        && Objects.equal(this.eventType, other.eventType)
        && Objects.equal(this.argumentType, other.argumentType)
        && Objects.equal(this.corefID, other.corefID);
  }

  public String toString() {
    return docID + "-" + eventType + "-" + argumentType + "-" + corefID;
  }
}
