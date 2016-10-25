package com.bbn.bue.common.xml;

import com.google.common.base.Optional;

import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static com.bbn.bue.common.xml.XMLUtils.nextSibling;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests XMLUtils.
 */
public class XMLUtilsTest {

  private static Element documentFromString(String source)
      throws IOException, SAXException, ParserConfigurationException {
    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    InputSource inputSource = new InputSource(new StringReader(source));
    return documentBuilder.parse(inputSource).getDocumentElement();
  }

  /**
   * Tests nextSibling.
   */
  @Test
  public void testNextSibling() throws ParserConfigurationException, SAXException, IOException {
    // Set up
    final String childTag = "child";
    final String parentWithChildren =
        "<parent><foo>foo</foo><child>child1</child><bar>bar</bar><child>child2</child><baz>baz</baz></parent>";
    final Element parent = documentFromString(parentWithChildren);
    final Element firstChild = (Element) parent.getFirstChild();

    // Check first childTag element
    Optional<Element> result = nextSibling(firstChild, childTag);
    assertTrue(result.isPresent());
    Element matchingChild = result.get();
    assertEquals("child1", matchingChild.getTextContent());

    // Check next childTag element
    result = nextSibling(matchingChild, childTag);
    assertTrue(result.isPresent());
    matchingChild = result.get();
    assertEquals("child2", matchingChild.getTextContent());

    // Check that there are no more
    result = nextSibling(matchingChild, childTag);
    assertFalse(result.isPresent());
  }
}
