package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.inject.AbstractModule;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A module which configured things from a parameter file.  By default, the identity of this module
 * (its hashCode and equals which are used to recognize duplicate modules in Guice) are determined
 * entirely by the parameters. If you desire other behavior, you must override hashCode and equals.
 *
 * @author Ryan Gabbard
 */
public abstract class AbstractParameterizedModule extends AbstractModule {

  private final Parameters parameters;

  protected AbstractParameterizedModule(final Parameters parameters) {
    this.parameters = checkNotNull(parameters);
  }

  // frequently these modules don't need to do any configuration,
  // so providing a default implementation reduces boilerplate
  @Override
  protected void configure() {

  }

  protected final Parameters params() {
    return parameters;
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameters);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final AbstractParameterizedModule other = (AbstractParameterizedModule) obj;
    return Objects.equals(this.parameters, other.parameters);
  }
}

