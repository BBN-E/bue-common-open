package com.bbn.nlp.corpora.eventNugget;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.xml.XMLUtils;
import com.bbn.nlp.corpora.ere.EREException;
import com.bbn.nlp.corpora.ere.ERESpan;

import com.google.common.base.Charsets;
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
 * @author Yee Seng Chan
 */
public final class EventNuggetLoader {

  private static Logger log = LoggerFactory.getLogger(EventNuggetLoader.class);

  private final Map<String, Object> idMap = Maps.newHashMap();


  private EventNuggetLoader() {
  }

  public static EventNuggetLoader from(final Parameters params) {
    return new EventNuggetLoader();
  }

  public NuggetDocument loadFrom(final File f) throws IOException {
    try {
      return loadFrom(Files.toString(f, Charsets.UTF_8));
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException(String.format("Error loading document %s", f.getAbsolutePath()), e);
    }
  }

  private NuggetDocument loadFrom(String s) throws IOException {
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

  private NuggetDocument loadFrom(org.w3c.dom.Document xml) {
    final Element root = xml.getDocumentElement();
    final String rootTag = root.getTagName();
    if (rootTag.equalsIgnoreCase("deft_ere_event_nuggets")) {

      final String docId = XMLUtils.requiredAttribute(root, "doc_id");
      final String sourceType = XMLUtils.requiredAttribute(root, "source_type");

      return toDocument(root, docId, sourceType);
    } else {
      throw new EREException("Light ERE should have a root of deft_ere");
    }
  }

  private NuggetDocument toDocument(final Element xml, final String docid,
      final String sourceType) {
    idMap.clear();

    final String kitId = generateID(XMLUtils.requiredAttribute(xml, "kit_id"), docid);

    final NuggetDocument.Builder documentBuilder =
        new NuggetDocument.Builder().kitId(kitId).docId(docid)
            .sourceType(NuggetDocument.SourceType.valueOf(sourceType));

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        Element e = (Element) child;

        if (e.getTagName().equals("hoppers")) {
          for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n instanceof Element) {
              if (((Element) n).getTagName().equals("hopper")) {
                documentBuilder.addHoppers(toHopper((Element) n, docid));
              }
            }
          }
        } else {
          throw new EREException("Unrecognized element in Document: " + e.getTagName());
        }
      }
    }

    final NuggetDocument result = documentBuilder.build();
    idMap.put(docid, result);

    return result;
  }


  private NuggetHopper toHopper(final Element xml, final String docid) {
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);

    final NuggetHopper.Builder hopperBuilder = new NuggetHopper.Builder().id(id);

    // read in event mentions
    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        Element e = (Element) child;
        if (e.getTagName().equals("event_mention")) {
          hopperBuilder.addEventMentions(toEventMention(e, docid));
        }
      }
    }

    final NuggetHopper hopper = hopperBuilder.build();
    idMap.put(id, hopper);
    return hopper;
  }

  private NuggetEventMention toEventMention(final Element xml, final String docid) {
    final String id = generateID(XMLUtils.requiredAttribute(xml, "id"), docid);
    final String type = XMLUtils.requiredAttribute(xml, "type");
    final String subtype = XMLUtils.requiredAttribute(xml, "subtype");
    final String realis = XMLUtils.requiredAttribute(xml, "realis");

    final NuggetEventMention.Builder emBuilder = new NuggetEventMention.Builder().id(id).type(
        Symbol.from(type)).subtype(Symbol.from(subtype)).realis(Symbol.from(realis));

    for (Node child = xml.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals("trigger")) {
          final ERESpan trigger = extractTrigger((Element) child, docid);
          emBuilder.trigger(trigger);
        }
      }
    }

    final NuggetEventMention eventMention = emBuilder.build();
    idMap.put(id, eventMention);
    return eventMention;
  }


  private ERESpan extractTrigger(final Element xml, final String docid) {
    final String source = XMLUtils.requiredAttribute(xml, "source");
    final String trigger = xml.getTextContent();
    final int offsetStart = Integer.parseInt(XMLUtils.requiredAttribute(xml, "offset"));
    final int offsetEnd =
        offsetStart + Integer.parseInt(XMLUtils.requiredAttribute(xml, "length")) - 1;

    return ERESpan.from(offsetStart, offsetEnd, trigger);
  }

  private String generateID(final String id, final String docid) {
    return docid + "-" + id;
  }

}
