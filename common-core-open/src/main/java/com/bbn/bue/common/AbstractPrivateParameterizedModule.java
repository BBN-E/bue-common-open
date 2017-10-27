package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.inject.PrivateModule;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Just like {@link AbstractParameterizedModule} except a {@link com.google.inject.PrivateModule}.
 * Remember, only things explicitly exposed will be visible outside a private module. See
 * the Guice documentation for details.
 *
 * @author Ryan Gabbard
 */
public abstract class AbstractPrivateParameterizedModule extends PrivateModule {

  private final Parameters parameters;

  protected AbstractPrivateParameterizedModule(final Parameters parameters) {
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
    final AbstractPrivateParameterizedModule other = (AbstractPrivateParameterizedModule) obj;
    return Objects.equals(this.parameters, other.parameters);
  }
}
