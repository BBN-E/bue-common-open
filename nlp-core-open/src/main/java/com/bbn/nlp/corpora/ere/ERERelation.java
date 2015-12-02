package com.bbn.nlp.corpora.ere;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public final class ERERelation implements Iterable<ERERelationMention> {
  private final String id;
  private final String type;
  private final String subtype;
  private final ImmutableList<ERERelationMention> relationMentions;


  private ERERelation(final String id, final String type, final String subtype, final List<ERERelationMention> relationMentions) {
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.subtype = checkNotNull(subtype);
    this.relationMentions = ImmutableList.copyOf(relationMentions);
  }


  @Override
  public Iterator<ERERelationMention> iterator() {
    return relationMentions.iterator();
  }

  public String getID() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getSubtype() {
    return subtype;
  }

  public ImmutableList<ERERelationMention> getRelationMentions() {
    return relationMentions;
  }

  public static Builder builder(final String id, final String type, final String subtype) {
    return new Builder(id, type, subtype);
  }
  
  public static class Builder {
    private final String id;
    private final String type;
    private final String subtype;
    private final List<ERERelationMention> relationMentions;

    private Builder(final String id, final String type, final String subtype) {
      this.id = checkNotNull(id);
      this.type = checkNotNull(type);
      this.subtype = checkNotNull(subtype);
      this.relationMentions = Lists.newArrayList();
    }

    public ERERelation build() {
      return new ERERelation(id, type, subtype, relationMentions);
    }

    public Builder withRelationMention(ERERelationMention rm) {
      this.relationMentions.add(rm);
      return this;
    }
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
    final ERERelation other = (ERERelation) obj;
    return Objects.equal(id, other.id);
  }
  
}

