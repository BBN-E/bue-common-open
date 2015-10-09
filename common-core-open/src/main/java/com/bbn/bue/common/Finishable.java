package com.bbn.bue.common;

import java.io.IOException;

/**
 * Something which needs to be notified when it is "finished" being used. This differs from {@link
 * java.io.Closeable} in not needing its {@codec close/finish} method to be idempotent.
 */
public interface Finishable {

  void finish() throws IOException;
}
