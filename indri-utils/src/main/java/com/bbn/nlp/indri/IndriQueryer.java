package com.bbn.nlp.indri;

import com.bbn.bue.common.scoring.Scored;

import java.util.List;

/**
 * Specifies a way of running queries against an existing Indri index.  If you call this code,
 * the Indri JNI libraries must be in your LD_LIBRARY_PATH or your program will crash.
 */
public interface IndriQueryer {

  public List<Scored<String>> docIDsMatchingQuery(String s);

  public List<Scored<String>> docIDsMatchingQuery(String s, int limit);

  public int countResults(String s) throws Exception;

}
