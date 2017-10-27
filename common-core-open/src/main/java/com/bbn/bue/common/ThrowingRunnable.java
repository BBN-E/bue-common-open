package com.bbn.bue.common;

/**
 * Like {@link Runnable}, but may throw an arbitrary exception.
 */
public interface ThrowingRunnable {

  void run() throws Exception;
}
