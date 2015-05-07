package com.bbn.nlp.corpora.ere;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

public final class EREEntity {
  private final String id;
  private final String type;
  private final String specificity;
  private final ImmutableList<EREEntityMention> mentions;

  private EREEntity(final String id, final String type, final String specificity, final List<EREEntityMention> mentions) {
    this.id = checkNotNull(id);
    this.type = checkNotNull(type);
    this.specificity = checkNotNull(specificity);
    this.mentions = ImmutableList.copyOf(mentions);
  }

  public String getID() {
    return id;
  }

  public String getType() {
    return type;
  }

  public String getSpecificity() {
    return specificity;
  }

  public ImmutableList<EREEntityMention> getMentions() {
    return mentions;
  }

  public static Builder builder(final String id, final String type, final String specificity) {
    return new Builder(id, type, specificity);
  }
  
  public static class Builder {
    private final String id;
    private final String type;
    private final String specificity;
    private final List<EREEntityMention> mentions;

    private Builder(final String id, final String type, final String specificity) {
      this.id = checkNotNull(id);
      this.type = checkNotNull(type);
      this.specificity = checkNotNull(specificity);
      this.mentions = Lists.newArrayList();
    }

    public EREEntity build() {
      return new EREEntity(id, type, specificity, mentions);
    }

    public Builder withMention(final EREEntityMention m) {
      mentions.add(m);
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
    final EREEntity other = (EREEntity) obj;
    return Objects.equal(id, other.id);
  }
}
