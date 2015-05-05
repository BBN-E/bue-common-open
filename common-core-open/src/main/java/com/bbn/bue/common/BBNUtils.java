package com.bbn.bue.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A few very BBN-specific utilities.
 */
public final class BBNUtils {

  private static final Logger log = LoggerFactory.getLogger(BBNUtils.class);

  private BBNUtils() {
    throw new UnsupportedOperationException();
  }

  public static void logCopyrightMessage() {
    log.info("Copyright 2015 Raytheon BBN Technologies Corp.\nAll rights reserved.");
  }
}
