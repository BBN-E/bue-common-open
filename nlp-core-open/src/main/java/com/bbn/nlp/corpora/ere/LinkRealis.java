package com.bbn.nlp.corpora.ere;

/**
 * Corresponds to the {@link EREEventMention}'s arguments link to the trigger of the mention.
 *
 * This corresponds to the "Event Argument Realis" section of the ERE Event Annotation guidelines.
 * As of version 2.9, this is section 4.4
 */
public enum LinkRealis {
  IRREALIS, // link realis is "false"
  REALIS // link realis is "true"
}
