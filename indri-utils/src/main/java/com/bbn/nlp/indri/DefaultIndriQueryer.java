package com.bbn.nlp.indri;

import com.bbn.bue.common.scoring.Scored;

import com.google.common.collect.ImmutableList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps Indri index in convenience methods.  Create instances using {@link
 * com.bbn.nlp.indri.Indri}.
 */
/* package-private */ final class DefaultIndriQueryer implements IndriQueryer {

  private final QueryEnvironment indriIndex;
  private final String docIDField;

  private static final int UNLIMITED = Integer.MAX_VALUE;

  private static Logger log = LoggerFactory.getLogger(DefaultIndriQueryer.class);

  public DefaultIndriQueryer(QueryEnvironment indriIndex, String docIDField) {
    this.indriIndex = checkNotNull(indriIndex);
    this.docIDField = checkNotNull(docIDField);
    checkArgument(!docIDField.isEmpty(), "May not have a null doc ID field for Indri queryer.");
  }

  @Override
  public List<Scored<String>> docIDsMatchingQuery(String query) {
    return docIDsMatchingQuery(query, UNLIMITED);
  }

  @Override
  public List<Scored<String>> docIDsMatchingQuery(String query, int limit) {
    final ImmutableList.Builder<Scored<String>> ret = ImmutableList.builder();

    try {
      final ScoredExtentResult[] indriResults = indriIndex.runQuery(query, limit);
      final String[] docIDs = indriIndex.documentMetadata(indriResults, docIDField);

      for (int i = 0; i < indriResults.length; ++i) {
        ret.add(Scored.from(docIDs[i], indriResults[i].score));
      }

      return ret.build();
    } catch (Exception e) {
      throw new IndriException("Exception while processing query: " + query, e);
    }
  }

  @Override
  public int countResults(String query) {
    // it can sometimes be significantly faster not to lookup the doc IDs
    try {
      return indriIndex.runQuery(query, UNLIMITED).length;
    } catch (Exception e) {
      throw new IndriException("Exception while processing query: " + query, e);
    }
  }
}
