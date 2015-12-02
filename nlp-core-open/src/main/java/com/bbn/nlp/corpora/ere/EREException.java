package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.xml.XMLUtils;

import org.w3c.dom.Element;

public class EREException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EREException(String msg) {
    super(msg);
  }

  public EREException(String msg, Throwable t) {
    super(msg, t);
  }

  public EREException(Element e, Throwable t) {
    super(String.format("While processing element %s", XMLUtils.dumpXMLElement(e)), t);
  }

  public EREException(String msg, Element e, Throwable t) {
    super(String.format("While processing element %s, %s", XMLUtils.dumpXMLElement(e), msg), t);
  }
}
