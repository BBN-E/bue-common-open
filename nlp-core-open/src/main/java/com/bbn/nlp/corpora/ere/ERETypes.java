package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.symbols.Symbol;

public final class ERETypes {

  public static final Symbol CONTACT_CORRESPONDANCE = Symbol.from("Contact.Correspondance");
  public static final Symbol MOVEMENT_TRANSPORT_ARTIFACT = Symbol.from(
      "Movement.Transport-Artifact");
  public static final Symbol MOVEMENT_TRANSPORT_PERSON = Symbol.from("Movement.Transport-Person");
  public static final Symbol MANUFACTURE_ARTIFACT = Symbol.from("Manufacture.Artifact");

  private ERETypes() {
    throw new UnsupportedOperationException();
  }
}
