package com.bbn.nlp.corpora.lightERE;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jay DeYoung
 */
public final class ERERelation {

  private final String id;
  private final TYPE type;
  private final SUBTYPE subtype;

  private final List<ERERelationMention> mentions;

  public ERERelation(final String id, final TYPE type, final SUBTYPE subtype,
      final List<ERERelationMention> mentions) {
    this.id = id;
    this.type = type;
    this.subtype = subtype;
    this.mentions = mentions;
  }

  public enum TYPE {
    affiliation,
    partwhole,
    physical,
    social,
  }

  public enum SUBTYPE {
    employment,
    leadership,
    membership,
    subsidiary,
    located,
    origin,
    business,
    family,
    role,
    unspecified,
    employmentmembership,
  }

  public String getId() {
    return id;
  }

  public TYPE getType() {
    return type;
  }

  public SUBTYPE getSubtype() {
    return subtype;
  }

  public List<ERERelationMention> getMentions() {
    return mentions;
  }

  @Override
  public String toString() {
    return "ERERelation{" +
        "id='" + id + '\'' +
        ", type=" + type +
        ", subtype=" + subtype +
        ", mentions=" + mentions +
        '}';
  }

  public static Builder builder(String id, String type, String subtype) {
    return new Builder(id, TYPE.valueOf(type), SUBTYPE.valueOf(subtype));
  }

  public static Builder builder(String id, TYPE type, SUBTYPE subtype) {
    return new Builder(id, type, subtype);
  }

  public static class Builder {

    private String id;
    private TYPE type;
    private SUBTYPE subtype;
    private List<ERERelationMention> mentions = Lists.newArrayList();

    public Builder(final String id,
        final TYPE type, final SUBTYPE subtype) {
      this.id = id;
      this.type = type;
      this.subtype = subtype;
    }

    public Builder setId(final String id) {
      this.id = id;
      return this;
    }

    public Builder setType(final TYPE type) {
      this.type = type;
      return this;
    }

    public Builder setSubtype(final SUBTYPE subtype) {
      this.subtype = subtype;
      return this;
    }

    public Builder withRelationMention(final ERERelationMention mention) {
      mentions.add(mention);
      return this;
    }

    public ERERelation build() {
      return new ERERelation(this.id, this.type, this.subtype, this.mentions);
    }
  }
}

