package com.bbn.nlp.corpora.ere;


import com.google.common.base.Optional;

public interface EREArgument {

  public String getID();

  public String getRole();

  public Optional<LinkRealis> getRealis();
}
