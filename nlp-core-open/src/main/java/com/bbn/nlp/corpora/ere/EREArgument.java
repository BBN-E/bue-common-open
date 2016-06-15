package com.bbn.nlp.corpora.ere;


import com.google.common.base.Optional;

public interface EREArgument {

  String getID();

  String getRole();

  ERESpan getExtent();

  Optional<LinkRealis> getRealis();
}
