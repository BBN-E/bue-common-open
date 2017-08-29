package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Qualifier;

import static org.junit.Assert.assertEquals;

public class AnnotationWrapperModuleTest {

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface Ann1 {

  }

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface Ann2 {

  }

  @Test
  public void testWrapSimpleBindings() {
    final Parameters params = Parameters.builder()
        .set("one", InnerModuleSimple1.class.getName())
        .set("two", InnerModuleSimple2.class.getName())
        .build();

    final Injector injector = Guice.createInjector(
        new AnnotationWrapperModule.Builder()
            .wrappedModuleParam("one")
            .wrappingAnnotation(Ann1.class)
            .wrap(String.class)
            .wrap(Pattern.class)
            .extractFrom(params),
        new AnnotationWrapperModule.Builder()
            .wrappingAnnotation(Ann2.class)
            .wrappedModuleParam("two")
            .wrap(String.class)
            .wrap(Pattern.class)
            .extractFrom(params));

    assertEquals("foo", injector.getInstance(Key.get(String.class, Ann1.class)));
    assertEquals("bar", injector.getInstance(Key.get(String.class, Ann2.class)));
    assertEquals("foo", injector.getInstance(Key.get(Pattern.class, Ann1.class)).pattern());
    assertEquals("bar", injector.getInstance(Key.get(Pattern.class, Ann2.class)).pattern());
  }

  @Test
  public void testWrapSetBindings() {
    final Parameters params = Parameters.builder()
        .set("one", InnerModuleWithSet1.class.getName())
        .set("two", InnerModuleWithSet2.class.getName())
        .build();

    final Injector injector = Guice.createInjector(
        new AnnotationWrapperModule.Builder()
            .wrappedModuleParam("one")
            .wrappingAnnotation(Ann1.class)
            .wrapSetOf(String.class)
            .extractFrom(params),
        new AnnotationWrapperModule.Builder()
            .wrappingAnnotation(Ann2.class)
            .wrappedModuleParam("two")
            .wrapSetOf(String.class)
            .extractFrom(params));

    assertEquals(ImmutableSet.of("foo", "bar"),
        injector.getInstance(Key.get(new TypeLiteral<Set<String>>() {
        }, Ann1.class)));
    assertEquals(ImmutableSet.of("oof", "rab"),
        injector.getInstance(Key.get(new TypeLiteral<Set<String>>() {
        }, Ann2.class)));
  }

  @Test
  public void testWrapMapBindings() {
    final Parameters params = Parameters.builder()
        .set("one", InnerModuleWithMap1.class.getName())
        .set("two", InnerModuleWithMap2.class.getName())
        .build();

    final Injector injector = Guice.createInjector(
        new AnnotationWrapperModule.Builder()
            .wrappedModuleParam("one")
            .wrappingAnnotation(Ann1.class)
            .wrapMapOf(String.class, Double.class)
            .extractFrom(params),
        new AnnotationWrapperModule.Builder()
            .wrappingAnnotation(Ann2.class)
            .wrappedModuleParam("two")
            .wrapMapOf(String.class, Double.class)
            .extractFrom(params));

    assertEquals(ImmutableMap.of("foo", 42.0d, "bar", 24.0d),
        injector.getInstance(Key.get(new TypeLiteral<Map<String, Double>>() {
        }, Ann1.class)));
    assertEquals(ImmutableMap.of("oof", 42.0d, "rab", 24.0d),
        injector.getInstance(Key.get(new TypeLiteral<Map<String, Double>>() {
        }, Ann2.class)));
  }


  @Test
  public void testMultibindings() {
    final Parameters params = Parameters.builder()
        .set("one", InnerModuleWithSetMultibinding1.class.getName()
            + "," + InnerModuleWithSetMultibinding2.class.getName())
        .build();

    final Injector injector = Guice.createInjector(
        new AnnotationWrapperModule.Builder()
            .wrappedModuleParam("one")
            .wrappingAnnotation(Ann1.class)
            .wrapSetOf(String.class)
            .extractFrom(params));

    // and we observe the multibindings are merged across modules appropriately
    assertEquals(ImmutableSet.of("foo", "bar", "oof", "rab"),
        injector.getInstance(Key.get(new TypeLiteral<Set<String>>() {
        }, Ann1.class)));
  }

}


class InnerModuleSimple1 extends AbstractModule {

  @Provides
  String getString() {
    return "foo";
  }

  @Provides
  Pattern getPattern() {
    return Pattern.compile("foo");
  }

  @Override
  protected void configure() {

  }
}

class InnerModuleSimple2 extends AbstractModule {

  @Provides
  String getString() {
    return "bar";
  }

  @Provides
  Pattern getPattern() {
    return Pattern.compile("bar");
  }

  @Override
  protected void configure() {

  }
}

class InnerModuleWithMap1 extends AbstractModule {

  @Override
  protected void configure() {
    final MapBinder<String, Double> mapBinder =
        MapBinder.newMapBinder(binder(), String.class, Double.class);

    mapBinder.addBinding("foo").toInstance(42d);
    mapBinder.addBinding("bar").toInstance(24d);
  }
}

class InnerModuleWithMap2 extends AbstractModule {

  @Override
  protected void configure() {
    final MapBinder<String, Double> mapBinder =
        MapBinder.newMapBinder(binder(), String.class, Double.class);

    mapBinder.addBinding("oof").toInstance(42d);
    mapBinder.addBinding("rab").toInstance(24d);
  }
}

class InnerModuleWithSet1 extends AbstractModule {

  @Override
  protected void configure() {
    final Multibinder<String> setBinder = Multibinder.newSetBinder(binder(), String.class);
    setBinder.addBinding().toInstance("foo");
    setBinder.addBinding().toInstance("bar");
  }
}

class InnerModuleWithSet2 extends AbstractModule {

  @Override
  protected void configure() {
    final Multibinder<String> setBinder = Multibinder.newSetBinder(binder(), String.class);
    setBinder.addBinding().toInstance("oof");
    setBinder.addBinding().toInstance("rab");
  }
}

class InnerModuleWithSetMultibinding1 extends AbstractModule {

  @Override
  protected void configure() {
    final Multibinder<String> setBinder = Multibinder.newSetBinder(binder(), String.class);
    setBinder.addBinding().toInstance("foo");
    setBinder.addBinding().toInstance("bar");
  }
}

class InnerModuleWithSetMultibinding2 extends AbstractModule {

  @Override
  protected void configure() {
    final Multibinder<String> setBinder = Multibinder.newSetBinder(binder(), String.class);
    setBinder.addBinding().toInstance("oof");
    setBinder.addBinding().toInstance("rab");
  }
}
