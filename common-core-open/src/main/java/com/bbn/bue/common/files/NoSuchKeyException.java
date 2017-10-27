package com.bbn.bue.common.files;

/**
 * Thrown to indicate that a non-existent key has been requested.
 *
 * @author Constantine Lignos, Ryan Gabbard
 */
public final class NoSuchKeyException extends RuntimeException {

  NoSuchKeyException() {
    super();
  }

  NoSuchKeyException(String message) {
    super(message);
  }

  NoSuchKeyException(String message, Throwable cause) {
    super(message, cause);
  }

  NoSuchKeyException(Throwable cause) {
    super(cause);
  }
}
