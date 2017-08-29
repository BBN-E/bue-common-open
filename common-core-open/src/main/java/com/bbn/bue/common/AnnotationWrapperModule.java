package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Types;

import org.immutables.value.Value;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A module which takes bindings from another module and exposes them with a given annotation.
 *
 * Suppose you have the need to bind entity type ontologies in your application for multiple
 * purposes and that these may differ.  To be concrete, suppose you want to bind a
 * {@code Set<EntityType>} annotated with an annotation {@code SourceP} and another such set
 * annotated with a different annotation {@code TargetP}.  You would rather not have to write
 * two sets of otherwise identical modules for each type ontology, one binding to each annotation.
 *
 * Instead, you can use this class.  It takes another module and installs it, but only exposes
 * from it the bindings requested with the specified {@link #wrappingAnnotation()} applied.
 *
 * To return to our example above, you could do:
 *
 * {@code
 * install(new AnnotationWrapperModule.Builder()
 * .wrappedModuleParam("sourceEntityOntology")
 * .wrappingAnnotation(SourceP.class)
 * .wrapSetOf(EntityType.class)
 * .extractFromParams(params));
 *
 * * install(new AnnotationWrapperModule.Builder()
 * .wrappedModuleParam("targetEntityOntology")
 * .wrappingAnnotation(TargetP.class)
 * .wrapSetOf(EntityType.class)
 * .extractFromParams(params));
 * }
 *
 * To make a wrapper module, use {@link Builder}.  You will need to specify:
 *
 * <ul>
 *
 * <li>{@link Builder#wrappingAnnotation(Class)}: the annotation which should be applied to
 * all the bindings exposed by this module.</li>
 *
 * <li>{@link Builder#wrappedModuleParam(String)}: a parameter giving the parameter from which
 * to read at runtime the fully-qualified name(s) of the module(s) to wrap.</li>
 *
 * <li>{@link Builder#wrap(Class)}, {@link Builder#wrap(Class, Class)},
 * {@link Builder#wrap(TypeLiteral, Class)}, {@link Builder#wrapSetOf(Class)},
 * and {@link Builder#wrapMapOf(Class, Class)}</li> to specify which things from the wrapped
 * module should be marked with an annotation and exposed.  The map and set methods will work
 * properly with multibindings. There are additional methods for use in more complex cases
 * (e.g. {@link Set}s of objects which are themselves generic.  Note that any annotations present
 * in the wrapped class will be lost, so care must be taken to avoid collisions in the
 * exposed mappings, which will result in an {@link IllegalArgumentException}.</li>
 *
 * Note that bindings have to be visible to the wrapper module to get wrapped, so bindings
 * done in wrapped private modules (or private modules within wrapped modules) will not get
 * wrapped.
 *
 * </ul>
 *
 * Finally, call {@link Builder#extractFrom(Parameters)} to create the module.
 *
 * @author Ryan Gabbard
 */
// we lose type safety doing this.  We're okay with it.
@SuppressWarnings("unchecked")
@TextGroupImmutable
@Value.Immutable
public abstract class AnnotationWrapperModule extends PrivateModule {

  abstract String wrappedModuleParam();

  abstract Class<? extends Annotation> wrappingAnnotation();

  abstract ImmutableSet<Key<?>> simpleTypesToWrap();

  abstract ImmutableSet<TypeLiteral<?>> setsToWrap();

  abstract ImmutableMap<TypeLiteral<?>, TypeLiteral<?>> mapsToWrap();

  abstract Parameters params();

  @Value.Check
  protected void check() {
    checkArgument(!wrappedModuleParam().isEmpty());

    // check for wrapped module bindings which will collide when we strip their own annotations
    // and replace them with the wrapping annotation
    final ImmutableSetMultimap.Builder<TypeLiteral<?>, Key<?>> externalTypesToInternalTypesB =
        ImmutableSetMultimap.builder();
    for (final Key<?> key : simpleTypesToWrap()) {
      externalTypesToInternalTypesB.put(key.getTypeLiteral(), key);
    }
    final ImmutableSetMultimap<TypeLiteral<?>, Key<?>> externalTypesToInternalTypes =
        externalTypesToInternalTypesB.build();

    for (final Map.Entry<TypeLiteral<?>, Collection<Key<?>>> e : externalTypesToInternalTypes
        .asMap().entrySet()) {
      if (e.getValue().size() > 1) {
        throw new IllegalArgumentException("You are exposing multiple inner module bindings which"
            + " will have the same external binding key: " + e.getValue());
      }
    }
  }

  /**
   * See Javadoc for {@link AnnotationWrapperModule}.
   */
  // warning is suppressed because we have many "unused" arguments which exist just to carry
  // type information
  @SuppressWarnings("unused")
  public static class Builder extends ImmutableAnnotationWrapperModule.Builder {

    /**
     * Request to expose an inner binding of type {@code T} as the external binding
     * {@code \@WrappingAnnotation T}.
     */
    public <T> Builder wrap(Class<T> clazz) {
      return addSimpleTypesToWrap(Key.get(clazz));
    }

    /**
     * Request to expose an inner binding of type {@code @InnerModuleAnnotation T} as the external
     * binding {@code \@WrappingAnnotation T}.
     */
    public <T> Builder wrap(Class<T> clazz, Class<? extends Annotation> innerModuleAnnotation) {
      return addSimpleTypesToWrap(Key.get(clazz, innerModuleAnnotation));
    }

    /**
     * Request to expose an inner binding of type {@code @InnerModuleAnnotation T} as the external
     * binding {@code \@WrappingAnnotation T}.
     */
    public <T> Builder wrap(TypeLiteral<T> typeLiteral,
        Class<? extends Annotation> innerModuleAnnotation) {
      return addSimpleTypesToWrap(Key.get(typeLiteral, innerModuleAnnotation));
    }

    /**
     * Request to expose an inner binding of type {@code Set<T>} as the external binding
     * {@code \@WrappingAnnotation Set<T>}.
     */
    public <T> Builder wrapSetOf(Class<T> clazz) {
      return addSetsToWrap(TypeLiteral.get(clazz));
    }

    /**
     * Request to expose an inner binding of type {@code Map<K,V>} as the external binding
     * {@code \@WrappingAnnotation Map<K,V>}.
     */
    public <K, V> Builder wrapMapOf(Class<K> key, Class<V> value) {
      return putMapsToWrap(TypeLiteral.get(key), TypeLiteral.get(value));
    }

    public Module extractFrom(Parameters params) {
      return params(params).build();
    }
  }

  @Override
  public void configure() {
    predeclareMultibindings();
    // the wrapped modules are installed, but not that this module itself is a PrivateModule,
    // so the bindings from the wrapped module will not be propagated beyond this module
    install(ModuleFromParameter.forMultiParameter(wrappedModuleParam()).extractFrom(params()));
    wrapAndExposeSimpleTypes();
    wrapAndExposeSets();
    wrapAndExposeMaps();
  }

  private void wrapAndExposeMaps() {
    for (final Map.Entry<TypeLiteral<?>, TypeLiteral<?>> mapItemType : mapsToWrap().entrySet()) {
      final Key<Object> innerModuleKey = (Key<Object>) (Key<?>) Key.get(
          mapOf(mapItemType.getKey(), mapItemType.getValue()));
      final Key<Object> keyToExpose = (Key<Object>) (Key<?>) Key.get(
          mapOf(mapItemType.getKey(), mapItemType.getValue()), wrappingAnnotation());
      bind(keyToExpose).to(innerModuleKey);
      expose(keyToExpose);
    }
  }

  private void wrapAndExposeSets() {
    for (final TypeLiteral<?> setItemType : setsToWrap()) {
      final Key<Object> innerModuleKey = (Key<Object>) (Key<?>) Key.get(setOf(setItemType));
      final Key<Object> keyToExpose =
          (Key<Object>) (Key<?>) Key.get(setOf(setItemType), wrappingAnnotation());
      bind(keyToExpose).to(innerModuleKey);
      expose(keyToExpose);
    }
  }

  private void wrapAndExposeSimpleTypes() {
    for (final Key<?> simpleType : simpleTypesToWrap()) {
      final Key<Object> innerModuleKey = (Key<Object>) simpleType;
      final TypeLiteral<?> innerTypeStrippedOfInnerAnnotation = simpleType.getTypeLiteral();
      final Key<Object> keyToExpose = (Key<Object>)
          Key.get(innerTypeStrippedOfInnerAnnotation, wrappingAnnotation());
      bind(keyToExpose).to(innerModuleKey);
      expose(keyToExpose);
    }
  }

  private void predeclareMultibindings() {
    // we "predeclare" the set and map bindings to ensure they exist as empty bindings even if
    // the wrapped module doesn't actually use them
    for (final TypeLiteral<?> setType : setsToWrap()) {
      Multibinder.newSetBinder(binder(), setType);
    }
    for (final Map.Entry<TypeLiteral<?>, TypeLiteral<?>> mapTypes : mapsToWrap().entrySet()) {
      MapBinder.newMapBinder(binder(), mapTypes.getKey(), mapTypes.getValue());
    }
  }

  // borrowed from Guice multibinding code
  @SuppressWarnings("unchecked")
  private static <T> TypeLiteral<Set<T>> setOf(TypeLiteral<T> elementType) {
    Type type = Types.setOf(elementType.getType());
    return (TypeLiteral<Set<T>>) TypeLiteral.get(type);
  }

  // from Guice's MapBinder
  @SuppressWarnings("unchecked")
  private static <K, V> TypeLiteral<Map<K, V>> mapOf(
      TypeLiteral<K> keyType, TypeLiteral<V> valueType) {
    return (TypeLiteral<Map<K, V>>) TypeLiteral.get(
        Types.mapOf(keyType.getType(), valueType.getType()));
  }
}
