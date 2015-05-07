package com.bbn.nlp.corpora.ere;


import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.xml.XMLUtils;
import com.bbn.nlp.corpora.ere.EREDocument;
import com.bbn.nlp.corpora.ere.EREEntity;
import com.bbn.nlp.corpora.ere.EREEntityArgument;
import com.bbn.nlp.corpora.ere.EREEntityMention;
import com.bbn.nlp.corpora.ere.EREEvent;
import com.bbn.nlp.corpora.ere.EREEventMention;
import com.bbn.nlp.corpora.ere.EREFiller;
import com.bbn.nlp.corpora.ere.EREFillerArgument;
import com.bbn.nlp.corpora.ere.ERERelation;
import com.bbn.nlp.corpora.ere.ERERelationMention;
import com.bbn.nlp.corpora.ere.ERESpan;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ERELoader {
  
  private static Logger log = LoggerFactory.getLogger(ERELoader.class);
  
  private final Map<String, Object> idMap;
  private final Map<String, String> mentionToCorefId;

  private ERELoader() {
    this.idMap = Maps.newHashMap();
    this.mentionToCorefId = Maps.newHashMap();
  }
  
  public static ERELoader from(final Parameters params) {
    return new ERELoader();
  }
  
  public EREDocument loadFrom(final File f) throws IOException {
    try {
      return loadFrom(Files.toString(f, Charsets.UTF_8));
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(String.format("Error loading ERE document %s", f.getAbsolutePath()), e);
    }
  }

  private EREDocument loadFrom(String s) throws IOException {
    // The XML parser treats \r\n as a single character. This is problematic
    // when we are using character offsets. To avoid this, we replace
    // \r with an entity reference before parsing
    final InputSource in = new InputSource(new StringReader(
        s.replaceAll("\r", "\n")));//"&#xD;")));
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      factory.setFeature("http://xml.org/sax/features/namespaces", false);
      factory.setFeature("http://xml.org/sax/features/validation", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return loadFrom(builder.parse(in));
    } catch (ParserConfigurationException e) {
      throw new EREException("Error parsing xml", e);
    } catch (SAXException e) {
      throw new EREException("Error parsing xml", e);
    }
  }

  private EREDocument loadFrom(org.w3c.dom.Document xml) {
    final Element root = xml.getDocumentElement();
    final String rootTag = root.getTagName();
    if (rootTag.equalsIgnoreCase("deft_ere")) {

      final String docId = XMLUtils.requiredAttribute(root, "doc_id");
      final String sourceType = XMLUtils.requiredAttribute(root, "source_type");

      final EREDocument doc = toDocument(root, docId, sourceType);  
      return doc;
    } else {
      throw new EREException("Rich ERE should have a root of deft_ere");
    }
  }
  
  private EREDocument toDocument(final Element xml, final String docid, final String sourceType) {
    idMap.clear();
    mentionToCorefId.clear();
    
    EREDocument.Builder builder = EREDocument.builder(docid, sourceType);

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        Element e = (Element) child;
        
        if (e.getTagName().equals("entities")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if(n instanceof Element) {
              if (((Element) n).getTagName().equals("entity")) {
                builder.withEntity(toEntity((Element)n, docid));
              }
            }
          }
        } else if (e.getTagName().equals("fillers")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if(n instanceof Element) {
              if (((Element) n).getTagName().equals("filler")) {
                builder.withFiller(toFiller((Element)n, docid));
              }
            }
          }
        } else if (e.getTagName().equals("relations")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if(n instanceof Element) {
              if (((Element) n).getTagName().equals("relation")) {
                builder.withRelation(toRelation((Element)n, docid));
              }
            }
          }
        } else if (e.getTagName().equals("hoppers")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if(n instanceof Element) {
              if (((Element) n).getTagName().equals("hopper")) {
                builder.withEvent(toEvent((Element)n, docid));
              }
            }
          }
        } else {
          throw new EREException("Unrecognized element in APF Document: " + e.getTagName());
        }
      }
    }

    EREDocument result = builder.build();
    idMap.put(docid, result);
    return result;
  }

  private EREEntity toEntity(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");    // ERE ids are not globally unique, so prefix with docid
    
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String specificity = XMLUtils.requiredAttribute(xml, "specificity");  // specific -> SPC , nonspecific -> GEN
    
    EREEntity.Builder builder = EREEntity.builder(id, type, specificity);

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("entity_mention")) {
          final EREEntityMention entityMention = toEntityMention((Element) child, docid);
          mentionToCorefId.put(entityMention.getID(), id);
          builder.withMention(entityMention);
        }
      }
    }

    final EREEntity entity = builder.build();
    idMap.put(id, entity);
    return entity;
  }

  private EREEntityMention toEntityMention(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");
    final String type = XMLUtils.requiredAttribute(xml, "noun_type");
    
    final int extentStart = XMLUtils.requiredIntegerAttribute(xml, "offset");
    final int extentEnd = extentStart + XMLUtils.requiredIntegerAttribute(xml, "length")-1;
    
    final ERESpan extent = toSpan(xml, "mention_text", extentStart, extentEnd).get();
    final Optional<ERESpan> head = toSpan(xml, "nom_head");
    
    final EREEntityMention mention = EREEntityMention.from(id, type, extent, head);
    idMap.put(id, mention);
    return mention;
  }

  // ==== Fillers and transforming them to APF entity/value/time ====
  private EREFiller toFiller(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final int extentStart = XMLUtils.requiredIntegerAttribute(xml, "offset");
    final int extentEnd = extentStart + XMLUtils.requiredIntegerAttribute(xml, "length")-1;
    final String text = xml.getTextContent();
    
    final ERESpan span = ERESpan.from(extentStart, extentEnd, text);
    
    final EREFiller ereFiller = EREFiller.from(id, type, span);
    idMap.put(id, ereFiller);
    return ereFiller;
  }
  /*
  private Symbol apfMentionType(final EREFiller ereFiller) {
    final String type = ereFiller.getType();
    
    if( "age".equals(type) || "crime".equals(type) || "money".equals(type) || 
        "sentence".equals(type) || "title".equals(type) || "url".equals(type)) {
      return VALUE;
    }
    else if("commodity".equals(type) || "vehicle".equals(type) || "weapon".equals(type)) {
      return ENTITY;
    }
    else if("time".equals(type)) {
      return TIME;
    }
    else {
      return NONE;
    }
  }
  
  private APFEntity toAPFEntity(final EREFiller ereFiller) {
    final APFEntityMention entityMention = APFEntityMention.from(ereFiller.getID()+"-1", 
        NONE.toString(), NONE.toString(), "FALSE", ereFiller.getExtent(), ereFiller.getHead());
    idMap.put(entityMention.getID(), entityMention);
    
    APFEntity.Builder builder = null;
    if("commodity".equals(ereFiller.getType())) {
      builder = APFEntity.builder(ereFiller.getID(), "Commodity", NONE.toString(), "SPC");
    }
    else if("vehicle".equals(ereFiller.getType())) {
      builder = APFEntity.builder(ereFiller.getID(), "VEH", NONE.toString(), "SPC");
    }
    else if("weapon".equals(ereFiller.getType())) {
      builder = APFEntity.builder(ereFiller.getID(), "WEA", NONE.toString(), "SPC");
    }
    
    if(builder!=null) {
      builder.withMention(entityMention);
      APFEntity entity = builder.build();
      idMap.put(entity.getID(), entity);
      mentionToCorefId.put(entityMention.getID(), ereFiller.getID());
      return entity;
    }
    else {
      return null;
    }
  }
  
  private APFValue toAPFValue(final EREFiller ereFiller) {
    final APFValueMention valueMention = APFValueMention.from(ereFiller.getID()+"-1", ereFiller.getExtent());
    idMap.put(valueMention.getID(), valueMention);
    
    APFValue value = null;
    if("age".equals(ereFiller.getType())) {
      value = APFValue.from(ereFiller.getID(), "Numeric", "NONE", valueMention);
    }
    else if("crime".equals(ereFiller.getType())) {
      value = APFValue.from(ereFiller.getID(), "Crime", "NONE", valueMention);
    }
    else if("money".equals(ereFiller.getType())) {
      value = APFValue.from(ereFiller.getID(), "Numeric", "Money", valueMention);
    }
    else if("sentence".equals(ereFiller.getType())) {
      value = APFValue.from(ereFiller.getID(), "Sentence", "NONE", valueMention);
    }
    else if("title".equals(ereFiller.getType())) {
      value = APFValue.from(ereFiller.getID(), "Job-Title", "NONE", valueMention);
    }
    else if("url".equals(ereFiller.getType())) {
      value = APFValue.from(ereFiller.getID(), "Contact-Info", "URL", valueMention);
    }
    
    if(value!=null) {
      idMap.put(value.getID(), value);
      mentionToCorefId.put(valueMention.getID(), ereFiller.getID());
    }
    
    return value;
  }
  
  private APFTime toAPFTime(final EREFiller ereFiller) {
    final APFTimeMention timeMention = APFTimeMention.from(ereFiller.getID()+"-1", ereFiller.getExtent());
    idMap.put(timeMention.getID(), timeMention);
    
    APFTime time = null;
    if("time".equals(ereFiller.getType())) {
      time = APFTime.from(ereFiller.getID(), "Time", timeMention);
    }
    
    if(time!=null) {
      idMap.put(time.getID(), time);
      mentionToCorefId.put(timeMention.getID(), ereFiller.getID());
    }
    
    return time;
  }
  // ==== END of Fillers ====
  */
  

  private static Optional<ERESpan> toSpan(final Element xml, final String name) {
    Optional<Element> element = XMLUtils.directChild(xml, name);
    if (element.isPresent()) {
      final int start = XMLUtils.requiredIntegerAttribute(element.get(), "offset");
      final int end = start + XMLUtils.requiredIntegerAttribute(element.get(), "length")-1;
      final String content = element.get().getTextContent();
      return Optional.of(ERESpan.from(start, end, content));
    }
    return Optional.<ERESpan>absent();
  }
  
  private static Optional<ERESpan> toSpan(final Element xml, final String name, final int start, final int end) {
    Optional<Element> element = XMLUtils.directChild(xml, name);
    if (element.isPresent()) {
      final String content = element.get().getTextContent();
      return Optional.of(ERESpan.from(start, end, content));
    }
    return Optional.<ERESpan>absent();
  }
  
  
  // ==== START Relation ====
  private ERERelation toRelation(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String subtype = XMLUtils.requiredAttribute(xml, "subtype");
    
    ERERelation.Builder builder = ERERelation.builder(id, type, subtype);
    
    // read in relation mentions
    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        Element e = (Element) child;
        if (e.getTagName().equals("relation_mention")) {
          builder.withRelationMention(toRelationMention(e, docid)); 
        }
      }
    }
    
    ERERelation relation = builder.build();
    idMap.put(id, relation);
    return relation;
  }
  
  private ERERelationMention toRelationMention(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");
    final String realis = XMLUtils.requiredAttribute(xml, "realis");
    
    final Optional<ERESpan> trigger = toSpan(xml, "trigger");
    
    ERERelationMention.Builder builder = ERERelationMention.builder(id, realis, trigger);
    
    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("rel_arg1") || ((Element) child).getTagName().equals("rel_arg2")) {
          Element e = (Element) child;
          final String tagName = e.getTagName();
          
          final String role = XMLUtils.requiredAttribute(e, "role");
          final Optional<String> entityMentionId = XMLUtils.optionalStringAttribute(e, "entity_mention_id");
          final Optional<String> fillerId = XMLUtils.optionalStringAttribute(e, "filler_id");

          String mentionId = docid + "-";
          if(entityMentionId.isPresent()) {
            mentionId += entityMentionId.get();
          }
          else if(fillerId.isPresent()) {
            mentionId += fillerId.get();
          }
            
          Object obj = fetch(mentionId);
          if(obj instanceof EREEntityMention) {
            final EREEntityMention m = (EREEntityMention) obj;
            builder.withArgument(tagName, EREEntityArgument.from(role, m));
          }
          else if(obj instanceof EREFiller) {
            final EREFiller m = (EREFiller) obj;
            builder.withArgument(tagName, EREFillerArgument.from(role, m));
          }  
        }
      }
    }
    
    ERERelationMention relationMention = builder.build();
    idMap.put(id, relationMention);
    return relationMention;
  }
  // ==== END Relation ====
  
  
  private EREEvent toEvent(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");
    
    EREEvent.Builder builder = EREEvent.builder(id);
    
    // read in event mentions
    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        Element e = (Element) child;
        if (e.getTagName().equals("event_mention")) {
          builder.withEventMention(toEventMention(e, docid)); 
        }
      }
    }
    
    EREEvent event = builder.build();
    idMap.put(id, event);
    return event;
  }
  
  private EREEventMention toEventMention(final Element xml, final String docid) {
    final String id = docid + "-" + XMLUtils.requiredAttribute(xml, "id");
      
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String subtype = XMLUtils.requiredAttribute(xml, "subtype");
    final String realis = XMLUtils.requiredAttribute(xml, "realis");
      
    final ERESpan trigger = toSpan(xml, "trigger").get();

    EREEventMention.Builder builder = EREEventMention.builder(id, type, subtype, realis, trigger);
    
    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("em_arg")) {
          Element e = (Element) child;
            
          final String role = XMLUtils.requiredAttribute(e, "role");
          final Optional<String> entityMentionId = XMLUtils.optionalStringAttribute(e, "entity_mention_id");
          final Optional<String> fillerId = XMLUtils.optionalStringAttribute(e, "filler_id");

          String mentionId = docid + "-";
          if(entityMentionId.isPresent()) {
            mentionId += entityMentionId.get();
          }
          else if(fillerId.isPresent()) {
            mentionId += fillerId.get();
          }
            
          Object obj = fetch(mentionId);
          if(obj instanceof EREEntityMention) {
            final EREEntityMention m = (EREEntityMention) obj;
            builder.withArgument(EREEntityArgument.from(role, m));
          }
          else if(obj instanceof EREFiller) {
            final EREFiller m = (EREFiller) obj;
            builder.withArgument(EREFillerArgument.from(role, m));
          }
        }
      }
    }

    EREEventMention eventMention = builder.build();
    idMap.put(id, eventMention);
    return eventMention;
  }  
    
    
  private <T> T fetch(final String id) {
    checkNotNull(id);
    checkArgument(!id.isEmpty());
    final T ret = (T) idMap.get(id);
    if (ret == null) {
      throw new EREException(String.format("Lookup failed for id %s.", id));
    }
    return ret;
  }

  /*
  private <T> T fetch(String attribute, Element e) {
    final String attVal = requiredAttribute(e, attribute);

    Object o = idMap.get(attVal);

    if (o == null) {
      throw new EREException(
          String.format("Lookup failed for id %s. Known keys are %s", attVal, idMap.keySet()));
    }

    try {
      return (T) o;
    } catch (ClassCastException f) {
      throw new EREException(String.format("Didn't expect ID %s to be %s", attVal, o.getClass()));
    }
  }
  */
  
}

  