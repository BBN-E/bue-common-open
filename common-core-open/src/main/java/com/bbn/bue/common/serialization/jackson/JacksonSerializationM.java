package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector;
import com.fasterxml.jackson.module.guice.GuiceInjectableValues;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.inject.Qualifier;

/**
 * Provides a {@link com.bbn.bue.common.serialization.jackson.JacksonSerializer.Builder}
 * whose resulting {@link JacksonSerializer} knows about Guice bindings.  The resulting
 * {@code Builder} may be safely modified.
 */
public final class JacksonSerializationM extends AbstractModule {
  @Override
  protected void configure() {
    final Key<Module> jacksonModulesKey = jacksonModulesKey();
    final Multibinder<Module> jacksonModulesBinder =
        Multibinder.newSetBinder(binder(), jacksonModulesKey).permitDuplicates();
    // go ahead and add two modules we always use, by default
    jacksonModulesBinder.addBinding().to(BUECommonOpenModule.class);
    jacksonModulesBinder.addBinding().toInstance(new GuavaModule());
  }

  private static final Key<Module> JACKSON_MODULES_KEY = Key.get(Module.class, JacksonModulesP.class);

  /**
   * Users can bind to this key with a {@link Multibinder} to guarantee Jackson modules are added.
   */
  public static Key<Module> jacksonModulesKey() {
    return JACKSON_MODULES_KEY;
  }

  @Provides
  public JacksonSerializer.Builder getJacksonSerializer(Injector injector,
      @JacksonModulesP Set<Module> jacksonModules) {
    final JacksonSerializer.Builder ret =
        JacksonSerializer.builder().withInjectionBindings(new GuiceAnnotationIntrospector(),
            new GuiceInjectableValues(injector));
    for (final Module jacksonModule : jacksonModules) {
      ret.registerModule(jacksonModule);
    }
    // we block this module from being installed if found on the classpath because it breaks
    // our normal Guice injection during deserialization.  This shouldn't be a problem because
    // things which use this (like Jersey) don't use JacksonSerializer.Builder to get their
    // deserializers anyway.  If it's ever a problem, we can provide an optional to disable it.
    ret.blockModuleClassName("com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule");
    return ret;
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

  /**
   * Bind to this with a multibinder to specify Jackson modules which should be installed
   * into injected serialziers and deserializers
   */
  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface JacksonModulesP {}

}
