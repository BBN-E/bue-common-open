package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * Provides a {@link com.bbn.bue.common.serialization.jackson.JacksonSerializer.Builder}
 * whose resulting {@link JacksonSerializer} knows about Guice bindings.  The resulting
 * {@code Builder} may be safely modified.
 */
public final class JacksonSerializationM extends AbstractModule {
  @Override
  protected void configure() {

  }

  @Provides
  public JacksonSerializer.Builder getJacksonSerializer(Injector injector) {
    return JacksonSerializer.builder().withInjectionBindings(new GuiceAnnotationIntrospector(),
        new GuiceInjectableValues(injector));
  }

  @Override
  public int hashCode() {
    // some arbitrary prime number
    return 1500450271;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }
}
