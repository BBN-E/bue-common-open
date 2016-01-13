package com.bbn.bue.common.xml;

import com.bbn.bue.common.symbols.Symbol;
import com.bbn.bue.common.symbols.SymbolUtils;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Some utilities for working with XML files.
 */
@Beta
public final class XMLUtils {

  private XMLUtils() {
    throw new UnsupportedOperationException();
  }

  public static boolean hasChildOfType(final Element e, final String name) {
    for (Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        if (((Element) child).getTagName().equals(name)) {
          return true;
        }
      }
    }
    return false;
  }

  public static Optional<Element> directChild(final Element parent, final String name) {
    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element && ((Element) child).getTagName().equalsIgnoreCase(name)) {
        return Optional.of((Element) child);
      }
    }
    return Optional.absent();
  }

  /**
   * Returns the element's next sibling with a tag matching the given name.
   *
   * @param element the element
   * @param name    the tag of the desired sibling
   * @return a sibling matching the tag, or {@link Optional#absent()} if there is none
   */
  public static Optional<Element> nextSibling(final Element element, final String name) {
    for (Node childNode = element.getNextSibling(); childNode != null;
         childNode = childNode.getNextSibling()) {
      if (childNode instanceof Element && ((Element) childNode).getTagName()
          .equalsIgnoreCase(name)) {
        return Optional.of((Element) childNode);
      }
    }
    return Optional.absent();
  }

  public static Element requiredDirectChild(final Element parent, final String name) {
    for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element && ((Element) child).getTagName().equalsIgnoreCase(name)) {
        return (Element) child;
      }
    }
    throw new XMLUnexpectedInputException(String
        .format("Parent with tag %s lacks required child element %s", parent.getTagName(), name));
  }

  public static Symbol requiredSymbolAttribute(final Node e, final String attribute) {
    return Symbol.from(requiredAttribute(e, attribute));
  }

  public static boolean requiredBooleanAttribute(final Node e, final String attribute) {
    final String val = requiredAttribute(e, attribute);

    try {
      return Boolean.parseBoolean(val);
    } catch (final NumberFormatException ex) {
      throw new XMLUnexpectedInputException(String
          .format("%s has required boolean attribute %s, but it doesn't parse as a boolean: %s",
              ((Element) e).getTagName(), attribute, e));
    }
  }

  public static float requiredFloatAttribute(final Node e, final String attribute) {
    final String val = requiredAttribute(e, attribute);

    try {
      return Float.parseFloat(val);
    } catch (final NumberFormatException ex) {
      throw new XMLUnexpectedInputException(
          String.format("%s has required float attribute %s, but it doesn't parse as a float: %s",
              ((Element) e).getTagName(), attribute, e));
    }
  }


  public static double requiredDoubleAttribute(final Node e, final String attribute) {
    final String val = requiredAttribute(e, attribute);

    try {
      return Double.parseDouble(val);
    } catch (final NumberFormatException ex) {
      throw new XMLUnexpectedInputException(
          String.format("%s has required double attribute %s, but it doesn't parse as a double: %s",
              ((Element) e).getTagName(), attribute, e));
    }
  }

  public static int requiredIntegerAttribute(final Node e, final String attribute) {
    final String val = requiredAttribute(e, attribute);

    try {
      return Integer.parseInt(val);
    } catch (final NumberFormatException ex) {
      throw new XMLUnexpectedInputException(
          String.format("%s has required int attribute %s, but it doesn't parse as an int : %s",
              ((Element) e).getTagName(), attribute, e));
    }
  }

  public static long requiredLongAttribute(final Node e, final String attribute) {
    final String val = requiredAttribute(e, attribute);

    try {
      return Long.parseLong(val);
    } catch (final NumberFormatException ex) {
      throw new XMLUnexpectedInputException(
          String.format("%s has required long attribute %s, but it doesn't parse as a long: %s",
              ((Element) e).getTagName(), attribute, e));
    }
  }

  public static String requiredAttribute(final Node node, final String attribute) {
    checkArgument(node instanceof Element);
    final Element e = (Element) node;
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return val;
    } else {
      throw new XMLUnexpectedInputException(
          String.format("%s missing required attribute %s: %s", e.getTagName(), attribute, e));
    }
  }

  public static void checkMissing(final Node e, final Object o, final String type) {
    checkArgument(e instanceof Element);
    if (o == null) {
      throw new XMLUnexpectedInputException(
          String.format("%s missing %s: %s", ((Element) e).getTagName(), type, e));
    }
  }

  public static boolean hasAnyChildElement(final Element e) {
    for (Node child = e.getFirstChild(); child != null; child = child.getNextSibling()) {
      if (child instanceof Element) {
        return true;
      }
    }
    return false;
  }

  public static Symbol symbolOrNull(final Element e, final String attribute) {
    final String val = e.getAttribute(attribute);
    if (val != null) {
      return Symbol.from(val);
    } else {
      return null;
    }
  }

  public static Symbol nonEmptySymbolOrNull(final Element e, final String attribute) {
    final String val = e.getAttribute(attribute);
    if (val != null && !val.isEmpty()) {
      return Symbol.from(val);
    } else {
      return null;
    }
  }

  public static boolean is(final Element e, final String tag) {
    return e.getTagName().equalsIgnoreCase(tag);
  }

  public static Optional<String> optionalStringAttribute(final Element e, final String attribute) {
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return Optional.of(val);
    } else {
      return Optional.absent();
    }
  }

  public static String defaultStringAttribute(final Element e, final String attribute,
      final String defaultValue) {
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return val;
    } else {
      return defaultValue;
    }
  }

  public static Optional<Integer> optionalIntegerAttribute(final Element e,
      final String attribute) {
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return Optional.of(Integer.parseInt(val));
    } else {
      return Optional.absent();
    }
  }

  public static Optional<Long> optionalLongAttribute(final Element e, final String attribute) {
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return Optional.of(Long.parseLong(val));
    } else {
      return Optional.absent();
    }
  }

  public static Optional<Symbol> optionalSymbolAttribute(final Element e, final String attribute) {
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return Optional.of(Symbol.from(val));
    } else {
      return Optional.absent();
    }
  }

  public static Optional<Double> optionalDoubleAttribute(final Element e, final String attribute) {
    final String val = e.getAttribute(attribute);

    if (!val.isEmpty()) {
      return Optional.of(Double.parseDouble(val));
    } else {
      return Optional.absent();
    }

  }

  public static List<Symbol> optionalSymbolList(final Element e, final String attribute,
      Splitter splitter) {
    final String val = e.getAttribute(attribute);

    return FluentIterable.from(splitter.split(val))
        .transform(SymbolUtils.Symbolize)
        .toList();
  }

  /**
   * @deprecated
   */
  @Deprecated
  public static List<Symbol> optionalSymbolList(final Element e, final String attribute) {
    return optionalSymbolList(e, attribute,
        Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults());
  }

  /**
   * @deprecated
   */
  @Deprecated
  public static List<Symbol> requiredSymbolList(final Element e, final String attribute) {
    return requiredSymbolList(e, attribute,
        Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults());
  }

  public static List<Symbol> requiredSymbolList(final Element e, final String attribute,
      Splitter splitter) {
    final String val = requiredAttribute(e, attribute);

    return FluentIterable.from(splitter.split(val))
        .transform(SymbolUtils.Symbolize)
        .toList();
  }

  /**
   * @deprecated
   */
  @Deprecated
  public static List<String> requiredStringList(final Element e, final String attribute) {
    return requiredStringList(e, attribute,
        Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults());
  }

  public static List<String> requiredStringList(final Element e, final String attribute,
      Splitter splitter) {
    final String val = requiredAttribute(e, attribute);

    return FluentIterable.from(splitter.split(val)).toList();
  }

  public static String dumpElement(final Element e) {
    return ((DOMImplementationLS) e.getOwnerDocument().getImplementation()).createLSSerializer()
        .writeToString(e);
  }

  public static abstract class FromXMLLoader<T> {

    public abstract T from(Element e);
  }

  public static final FromXMLLoader<String> ToContentString = new FromXMLLoader<String>() {
    @Override
    public String from(final Element e) {
      return e.getTextContent();
    }
  };

  public static <T> List<T> childrenToList(final Element e, final FromXMLLoader<T> childToXML) {
    return childrenToList(e, null, childToXML);
  }

  public static <T> List<T> childrenToList(final Element e, final String kidName,
      final FromXMLLoader<T> childToXML) {
    return childrenToListInternal(e, kidName, childToXML, true);
  }

  private static <T> List<T> childrenToListInternal(final Element e,
      final String kidName, final FromXMLLoader<T> childToXML, final boolean throwOnMismatch) {
    final List<T> list = Lists.newArrayList();

    for (Node kid = e.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
      if (kid instanceof Element) {
        final Element kidElement = (Element) kid;
        if (kidName == null || is(kidElement, kidName)) {
          list.add(childToXML.from(kidElement));
        } else if (throwOnMismatch) {
          throw new XMLUnexpectedInputException(String
              .format("Expected children of type %s but encountered %s", kidName,
                  kidElement.getTagName()));
        }
      }
    }

    return list;
  }

  public static <T> List<T> matchingChildrenToList(final Element e,
      final String kidName, final FromXMLLoader<T> childToXML) {
    return childrenToListInternal(e, kidName, childToXML, false);
  }

  public static boolean emptyElement(final Element e) {
    return !e.hasChildNodes();
  }

  public static Element requiredSingleChild(final Element e, final String childName) {
    Element singleChild = null;

    for (Node kid = e.getFirstChild(); kid != null; kid = kid.getNextSibling()) {
      if (kid instanceof Element) {
        final Element kidElement = (Element) kid;
        if (is(kidElement, childName)) {
          if (singleChild != null) {
            throw new XMLUnexpectedInputException(
                String.format("Expected a single child of type %s but found multple.", childName));
          }
          singleChild = kidElement;
        }
      }
    }

    if (singleChild == null) {
      throw new XMLUnexpectedInputException(
          String.format("Expected child of type %s but didn't find one", childName));
    }

    return singleChild;
  }

  public static <T> T requiredSingleChild(final Element e, final String childName,
      final FromXMLLoader<T> loader) {
    return loader.from(requiredSingleChild(e, childName));
  }

  private static Transformer dumpTransformer = null;

  public static String dumpXMLElement(final Element e) {
    if (dumpTransformer == null) {
      try {
        dumpTransformer = TransformerFactory.newInstance().newTransformer();
      } catch (final TransformerConfigurationException e1) {
        throw new XMLException("XML configuration problem", e1);
      } catch (final TransformerFactoryConfigurationError e1) {
        throw new XMLException("XML configuration problem", e1);
      }
      dumpTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    }
    final StringWriter out = new StringWriter();
    try {
      dumpTransformer.transform(new DOMSource(e), new StreamResult(out));
    } catch (final TransformerException e1) {
      throw new XMLException("XML configuration problem", e1);
    }
    return out.toString();
  }

  /**
   * Returns an {@link Iterable} over all children of {@code e} with tag {@code tag}
   */
  public static Iterable<Element> childrenWithTag(Element e, String tag) {
    return new ElementChildrenIterable(e, tag);
  }

  private static final class ElementChildrenIterable implements Iterable<Element> {

    private final Element e;
    private final String tagName;

    public ElementChildrenIterable(final Element e, final String tagName) {
      this.e = checkNotNull(e);
      this.tagName = checkNotNull(tagName);
    }

    @Override
    public Iterator<Element> iterator() {
      return new ElementChildrenIterator();
    }

    private final class ElementChildrenIterator extends AbstractIterator<Element> {

      Node curNode = e.getFirstChild();

      @Override
      protected Element computeNext() {
        if (curNode == null) {
          return endOfData();
        }
        while (curNode != null) {
          if (curNode instanceof Element) {
            final Element curElement = ((Element) curNode);
            curNode = curNode.getNextSibling();
            if (curElement.getTagName().equals(tagName)) {
              return curElement;
            }
          } else {
            curNode = curNode.getNextSibling();
          }
        }
        return endOfData();
      }
    }
  }
}
