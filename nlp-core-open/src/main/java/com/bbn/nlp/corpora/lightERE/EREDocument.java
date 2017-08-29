package com.bbn.nlp.corpora.lightERE;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jay DeYoung
 */
public final class EREDocument {

  // xml entities
  private final List<EREEntity> entities;
  private final List<ERERelation> relations;
  private final List<EREEvent> events;

  // xml attributes
  private final String kit_id;
  private final String docid;
  private final Optional<String> conversation_id;
  private final SourceType source_type;

  public EREDocument(final List<EREEntity> entities, final List<ERERelation> relations,
      final List<EREEvent> events, final String kit_id, final String docid,
      final Optional<String> conversation_id, final SourceType source_type) {
    this.entities = entities;
    this.relations = relations;
    this.events = events;
    this.kit_id = kit_id;
    this.docid = docid;
    this.conversation_id = conversation_id;
    this.source_type = source_type;
  }


  public List<EREEntity> getEntities() {
    return entities;
  }

  public List<ERERelation> getRelations() {
    return relations;
  }

  public List<EREEvent> getEvents() {
    return events;
  }

  public String getKit_id() {
    return kit_id;
  }

  public String getDocid() {
    return docid;
  }

  public Optional<String> getConversation_id() {
    return conversation_id;
  }

  public SourceType getSource_type() {
    return source_type;
  }

  public enum SourceType {
    narrative,
    conversation,
    multi_post
  }

  @Override
  public String toString() {
    return "EREDocument{" +
        "entities=" + entities +
        ", relations=" + relations +
        ", events=" + events +
        ", kit_id='" + kit_id + '\'' +
        ", docid=" + docid +
        ", conversation_id=" + conversation_id +
        ", source_type=" + source_type +
        '}';
  }

  public static Builder builder(String docid, String source_type) {
    return new Builder(docid, SourceType.valueOf(source_type));
  }

  public static Builder builder(String docid, SourceType source_type) {
    return new Builder(docid, source_type);
  }

  public static class Builder {

    private List<EREEntity> entities = Lists.newArrayList();
    private List<ERERelation> relations = Lists.newArrayList();
    private List<EREEvent> events = Lists.newArrayList();
    private String kit_id;
    private String docid;
    private Optional<String> conversation_id;
    private SourceType source_type;

    private Builder(String docid, SourceType sourceType) {
      this.docid = docid;
      this.source_type = sourceType;
    }

    public Builder setKit_id(final String kit_id) {
      this.kit_id = kit_id;
      return this;
    }

    public Builder setDocid(final String docid) {
      this.docid = docid;
      return this;
    }

    public Builder setConversation_id(final Optional<String> conversation_id) {
      this.conversation_id = conversation_id;
      return this;
    }

    public EREDocument build() {
      return new EREDocument(this.entities, this.relations, this.events, this.kit_id, this.docid,
          this.conversation_id, this.source_type);
    }

    public void withEntity(final EREEntity ereEntity) {
      this.entities.add(ereEntity);
    }

    public void withRelation(final ERERelation ereRelation) {
      this.relations.add(ereRelation);
    }

    public void withEvent(final EREEvent ereEvent) {
      this.events.add(ereEvent);
    }
  }
}

