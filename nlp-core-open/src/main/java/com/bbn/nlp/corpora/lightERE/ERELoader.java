package com.bbn.nlp.corpora.lightERE;


import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.xml.XMLUnexpectedInputException;
import com.bbn.bue.common.xml.XMLUtils;
import com.bbn.nlp.corpora.ere.EREException;

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

/**
 * shamelessly stolen/borrowed from the richERE package com.bbn.nlp.corpora.ere
 *
 * @author Jay DeYoung
 */
public class ERELoader {

  private static Logger log = LoggerFactory.getLogger(ERELoader.class);

  private final Map<String, Object> idMap;
  private final Map<String, String> mentionToCorefId;

  private int entities;
  private int entity_mentions;
  private int relations;
  private int relation_mentions;

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

      final String docId = XMLUtils.requiredAttribute(root, "docid");
      final String sourceType = XMLUtils.requiredAttribute(root, "source_type");

      final EREDocument doc = toDocument(root, docId, sourceType);
      return doc;
    } else {
      throw new EREException("Light ERE should have a root of deft_ere");
    }
  }

  private EREDocument toDocument(final Element xml, final String docid, final String sourceType) {
    idMap.clear();
    mentionToCorefId.clear();
    entities = entity_mentions = relation_mentions = relations = 0;

    final EREDocument.Builder builder = EREDocument.builder(docid, sourceType);
    final String kit_id = generateID(XMLUtils.requiredAttribute(xml, "kit_id"), docid);
    final Optional<String> conversation_id =
        XMLUtils.optionalStringAttribute(xml, "conversation_id");
    builder.setKit_id(kit_id);
    builder.setConversation_id(conversation_id);

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        Element e = (Element) child;

        if (e.getTagName().equals("entities")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
              if (((Element) n).getTagName().equals("entity")) {
                builder.withEntity(toEntity((Element) n, docid));
              }
            }
          }
        } else if (e.getTagName().equals("events")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
              if (((Element) n).getTagName().equals("event")) {
                builder.withEvent(toEvent((Element) n, docid));
              }
            }
          }
        } else if (e.getTagName().equals("relations")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
              if (((Element) n).getTagName().equals("relation")) {
                builder.withRelation(toRelation((Element) n, docid));
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
    log.info(
        "reading in doc {}, found {} entities {} entity mentions {} relations {} relation mentions",
        docid, entities, entity_mentions, relations, relation_mentions);
    return result;
  }

  private EREEntity toEntity(final Element xml, final String docid) {
    entities++;
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String name = XMLUtils.requiredAttribute(xml, "name");

    EREEntity.Builder builder = EREEntity.builder(id, type);
    builder.setName(name);

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("entity_mention")) {
          final EREEntityMention entityMention = toEntityMention((Element) child, docid);
//          mentionToCorefId.put(entityMention.getID(), id);
          builder.withMention(entityMention);
        }
      }
    }

    final EREEntity entity = builder.build();
    idMap.put(id, entity);
    return entity;
  }

  private EREEntityMention toEntityMention(final Element xml, final String docid) {
    entity_mentions++;
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);
    final String type = XMLUtils.requiredAttribute(xml, "noun_type");
    final String source = XMLUtils.requiredAttribute(xml, "source");

    final int offset = Integer.parseInt(XMLUtils.requiredAttribute(xml, "offset"));
    final int length = Integer.parseInt(XMLUtils.requiredAttribute(xml, "length"));
    final String text = xml.getTextContent();

    final EREEntityMention mention = EREEntityMention.from(id, type, source, offset, length, text);
//    idMap.put(id, mention);
    return mention;
  }

  // ==== START Relation ====
  private ERERelation toRelation(final Element xml, final String docid) {
    relations++;
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);
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
    relation_mentions++;
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);

    ERERelationMention.Builder builder = ERERelationMention.builder(id);

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("arg")) {
          final EREArg arg = extractArg((Element) child, docid);
          builder.withArg(arg);
        }
        if (((Element) child).getTagName().equals("trigger")) {
          builder.setTrigger(extractPossiblyAbsentTrigger((Element) child, docid));
        }

      }
    }

    ERERelationMention relationMention = builder.build();
    idMap.put(id, relationMention);
    return relationMention;
  }
  // ==== END Relation ====

  private Optional<ERETrigger> extractPossiblyAbsentTrigger(final Element xml, final String docid) {
    try {
      return Optional.of(extractTrigger(xml, docid));
    } catch (XMLUnexpectedInputException e) {
      e.printStackTrace();
      log.info("no trigger found for {}, error is {}", xml, e.toString());
    }
    return Optional.absent();
  }


  private EREEvent toEvent(final Element xml, final String docid) {
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);
    final String name = XMLUtils.requiredAttribute(xml, "name");

    EREEvent.Builder builder = EREEvent.builder(id, name);

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
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);

    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String subtype = XMLUtils.requiredAttribute(xml, "subtype");

    EREEventMention.Builder builder = EREEventMention.builder(id, type, subtype);

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("trigger")) {
          final Optional<ERETrigger> trigger = extractPossiblyAbsentTrigger((Element) child, docid);
          if (trigger.isPresent()) {
            builder.setTrigger(trigger.get());
          } else {
            log.error("missing trigger for docid {}, element {}", docid, child);
          }
        }
        if (((Element) child).getTagName().equals("args")) {
          for (Node arg = child.getFirstChild(); arg != null; arg = arg.getNextSibling()) {
            if (arg instanceof Element && ((Element) arg).getTagName().equals("arg")) {
              builder.withArg(extractArg((Element) arg, docid));
            } else {
            }
          }
        }
        if (((Element) child).getTagName().equals("places")) {
          for (Node place = child.getFirstChild(); place != null; place = place.getNextSibling()) {
            if (place instanceof Element && ((Element) place).getTagName().equals("place")) {
              builder.withPlace(extractPlace((Element) place, docid));
            } else {
            }
          }
        }
        if (((Element) child).getTagName().equals("date")) {
          builder.setDate(Optional.of(extractDate((Element) child, docid)));
        }
      }
    }

    EREEventMention eventMention = builder.build();
    idMap.put(id, eventMention);
    return eventMention;
  }

  private EREDate extractDate(final Element xml, final String docid) {
    EREDate.Builder builder = EREDate.builder();
    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element && ((Element) child).getTagName().equals("date_extent")) {
        final String source = XMLUtils.requiredAttribute(child, "source");
        final int offset = Integer.parseInt(XMLUtils.requiredAttribute(child, "offset"));
        final int length = Integer.parseInt(XMLUtils.requiredAttribute(child, "length"));
        final String content = child.getTextContent();
        builder.setDateExtent(EREDateExtent.from(source, offset, length, content));
      }
      if (child instanceof Element && ((Element) child).getTagName().equals("normalized_date")) {
        final String date = child.getTextContent();
        builder.setNormalizedDate(ERENormalizedDate.from(date));
      }
    }
    return builder.build();
  }

  private EREPlace extractPlace(final Element xml, final String docid) {
    final Optional<String> type = XMLUtils.optionalStringAttribute(xml, "type");
    final String entity_mention_id = generateID(
        XMLUtils.requiredAttribute(xml, "entity_mention_id"), docid);
    return EREPlace.from(type, entity_mention_id);
  }

  private EREArg extractArg(final Element xml, final String docid) {
    final String entity_mention_id =
        generateID(XMLUtils.requiredAttribute(xml, "entity_mention_id"), docid);
    final String entity_id = generateID(XMLUtils.requiredAttribute(xml, "entity_id"), docid);
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String text = xml.getTextContent();
    return EREArg.from(entity_mention_id, entity_id, type, text);
  }

  private ERETrigger extractTrigger(final Element xml, final String docid) {
    final String source = XMLUtils.requiredAttribute(xml, "source");
    final String trigger = xml.getTextContent();
    final int offset = Integer.parseInt(XMLUtils.requiredAttribute(xml, "offset"));
    final int length = Integer.parseInt(XMLUtils.requiredAttribute(xml, "length"));
    return ERETrigger.from(source, trigger, offset, length);
  }

  private String generateID(final String id, final String docid) {
    return docid + "-" + id;
  }

}

