package com.bbn.nlp.corpora.ere;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.List;

public final class EREDocument {
  private final String docid;
  private final String sourceType;
  private final List<EREEntity> entities;
  private final List<EREFiller> fillers;
  private final List<ERERelation> relations;
  private final List<EREEvent> events;

  private EREDocument(final String docid, final String sourceType, 
      final List<EREEntity> entities, final List<EREFiller> fillers,  
      final List<ERERelation> relations, final List<EREEvent> events) {
    this.docid = checkNotNull(docid);
    this.sourceType = checkNotNull(sourceType);
    this.entities = ImmutableList.copyOf(entities);
    this.fillers = ImmutableList.copyOf(fillers);
    this.relations = ImmutableList.copyOf(relations);
    this.events = ImmutableList.copyOf(events);
  }

  public String getDocId() {
    return docid;
  }

  public String getSourceType() {
    return sourceType;
  }
  
  public List<EREEntity> getEntities() {
    return entities;
  }

  public List<EREFiller> getFillers() {
    return fillers;
  }
  
  public List<ERERelation> getRelations() {
    return relations;
  }

  public List<EREEvent> getEvents() {
    return events;
  }
  
  public static Builder builder(final String docid, final String sourceType) {
    return new Builder(docid, sourceType);
  }
  
  public static class Builder {
    private final String docid;
    private final String sourceType;
    private final List<EREEntity> entities;
    private final List<EREFiller> fillers;
    private final List<ERERelation> relations;
    private final List<EREEvent> events;
    
    public Builder(final String docid, final String sourceType) {
      this.docid = checkNotNull(docid);
      this.sourceType = checkNotNull(sourceType);
      this.entities = Lists.newArrayList();
      this.fillers = Lists.newArrayList();
      this.relations = Lists.newArrayList();
      this.events = Lists.newArrayList();
    }

    public EREDocument build() {
      return new EREDocument(docid, sourceType, entities, fillers, relations, events);
    }

    public Builder withEntity(EREEntity e) {
      this.entities.add(e);
      return this;
    }

    public Builder withFiller(EREFiller v) {
      this.fillers.add(v);
      return this;
    }
    
    public Builder withRelation(ERERelation r) {
      this.relations.add(r);
      return this;
    }

    public Builder withEvent(EREEvent e) {
      this.events.add(e);
      return this;
    }
    
  }

  public static String formatText(String text) {
    return text.replaceAll("\\n+", "").replace("&", " &amp; ");
  }

  public void writeToAPFfile(String strFileApf, String strFileSgm) throws IOException {
    /*
    StringBuffer s = new StringBuffer("");
    
    PrintWriter pwApf = new PrintWriter(new OutputStreamWriter(new FileOutputStream(strFileApf), "UTF-8"));
        
    s.append("<document DOCID=\"" + docid + "\">\n");

    // entity
    for(final APFEntity entity : entities) {
      s.append("<entity ID=\"" + entity.getID() + "\" TYPE=\"" + entity.getType() + 
          "\" SUBTYPE=\"" + entity.getSubtype() + "\" CLASS=\"" + entity.getCls() + "\">\n");

      for(final APFEntityMention entityMention : entity.getMentions()) {
        s.append(Strings.repeat(" ", 2) + "<entity_mention ID=\"" + entityMention.getID() + 
            "\" TYPE=\"" + entityMention.getType() + "\" LDCTYPE=\"" + entityMention.getLdcType() + 
            "\" LDCATR=\"" + entityMention.getLdcAtr() + "\">\n");

        s.append(Strings.repeat(" ", 4) + "<extent>\n");
        
        s.append(Strings.repeat(" ", 6) + "<charseq START=\"" + entityMention.getExtent().getStart() + "\" END=\""
            + entityMention.getExtent().getEnd() + "\">" +
            formatText(entityMention.getExtent().getText()) + "</charseq>\n";
        
        s.append(Strings.repeat(" ", 4) + "</extent>\n");

        s.append(Strings.repeat(" ", 4) + "<head>\n");
        
        s.append(Strings.repeat(" ",  6) + "<charseq START=\"" + entityMention.getHead().getStart() + "\" END=\""
            + entityMention.getHead().getEnd() + "\">" +
            formatText(entityMention.getHead().getText()) + "</charseq>\n";
        
        s.append(Strings.repeat(" ", 4) + "</head>\n");

        s.append(Strings.repeat(" ", 2) + "</entity_mention>\n");
      }

      s.append("</entity>\n");
    }

    for (APFValue value : values) {
      APFValueMention valueMention = value.getValueMention();
      strAceDoc +=
          "<value ID=\"" + value.getID() + "\" TYPE=\"" + value.getType() + "\" SUBTYPE=\"" + value
              .getSubtype() + "\">\n";
      strAceDoc += "  <value_mention ID=\"" + valueMention.getID() + "\">\n";
      strAceDoc += "    <extent>\n";
      strAceDoc += "      <charseq START=\"" + valueMention.getExtent().getStart() + "\" END=\""
          + valueMention.getExtent().getEnd() + "\">" +
          formatText(valueMention.getExtent().getText()) + "</charseq>\n";
      strAceDoc += "    </extent>\n";
      strAceDoc += "  </value_mention>\n";
      strAceDoc += "</value>\n";
    }

    for (APFRelation relation : relations) {
      strAceDoc +=
          "<relation ID=\"" + relation.getID() + "\" TYPE=\"" + relation.getType() + "\" SUBTYPE=\""
              + relation.getSubtype() +
              "\" TENSE=\"" + relation.getTense() + "\" MODALITY=\"" + relation.getModality()
              + "\">\n";

      for (String role : new TreeSet<String>((relation.getRoles()))) {
        APFMentionIterable<? extends APFSpanning> arg = relation.getArgument(role).get();
        strAceDoc += "  <relation_argument REFID=\"" + arg.getID() + "\" ROLE=\"" + role + "\"/>\n";
      }

      for (APFRelationMention relMention : relation.getRelationMentions()) {
        strAceDoc +=
            "  <relation_mention ID=\"" + relMention.getID() + "\" LEXICALCONDITION=\"" + relMention
                .getLexicalCondition() + "\">\n";
        if (relMention.getExtent().isPresent()) {
          strAceDoc += "    <extent>\n";
          strAceDoc +=
              "      <charseq START=\"" + relMention.getExtent().get().getStart() + "\" END=\""
                  + relMention.getExtent().get().getEnd() + "\">" +
                  formatText(relMention.getExtent().get().getText()) + "</charseq>\n";
          strAceDoc += "    </extent>\n";
        }

        for (String role : new TreeSet<String>(relMention.getRoles())) {
          APFRelationMentionArgument relMenArg = relMention.getArgument(role).get();
          strAceDoc += "    <relation_mention_argument REFID=\"" + relMenArg.getMention().getID()
              + "\" ROLE=\"" + role + "\">\n";
          if (relMenArg.getExtent().isPresent()) {
            strAceDoc += "      <extent>\n";
            strAceDoc +=
                "        <charseq START=\"" + relMenArg.getExtent().get().getStart() + "\" END=\""
                    + relMenArg.getExtent().get().getEnd() + "\">" +
                    formatText(relMenArg.getExtent().get().getText()) + "</charseq>\n";
            strAceDoc += "      </extent>\n";
          }
          strAceDoc += "    </relation_mention_argument>\n";
        }

        strAceDoc += "  </relation_mention>\n";
      }
      strAceDoc += "</relation>\n";
    }

    strAceDoc += "</document>\n" + "</source_file>\n";

    pwApf.print(strAceDoc);
    pwApf.close();
    */
  }
}
