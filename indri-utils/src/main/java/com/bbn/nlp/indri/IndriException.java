package com.bbn.nlp.indri;

/**
 * Indicates an exception thrown during Indri processing. Be aware that since Indri queries
 * are processed in native code, exceptional situations may occur which will crash the JVM
 * rather than than exception.
 */
public final class IndriException extends RuntimeException {

  public IndriException(final Throwable cause) {
    super(cause);
  }

  public IndriException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
