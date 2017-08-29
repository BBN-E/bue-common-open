package com.bbn.bue.common;

import com.google.inject.AbstractModule;

/**
 * A module which does nothing. It is sometimes used as a default for {@link ModuleFromParameter}.
 *
 * @author Ryan Gabbard
 */
public final class NoOpModule extends AbstractModule {

  @Override
  protected void configure() {

  }
}
