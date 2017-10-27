package com.bbn.bue.common;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Declares a map-based multibinding injection site, but doesn't put anything in the map. This is
 * useful as the default module for {@link ModuleFromParameter} when you have a plug-in architecture
 * based on {@link MapBinder} but no plugins are specified.
 *
 * @author Ryan Gabbard
 */
public final class EmptyMapBindingM extends AbstractModule {

  private final TypeLiteral<?> keyType;
  private final TypeLiteral<?> valueType;
  @Nullable
  private final Class<? extends Annotation> annotation;

  private EmptyMapBindingM(final TypeLiteral<?> keyType, final TypeLiteral<?> valueType,
      @Nullable final Class<? extends Annotation> annotation) {
    this.keyType = checkNotNull(keyType);
    this.valueType = checkNotNull(valueType);
    this.annotation = annotation;
  }

  public static EmptyMapBindingM forKeyValueTypes(final TypeLiteral<?> keyType,
      final TypeLiteral<?> valueType) {
    return new EmptyMapBindingM(keyType, valueType, null);
  }

  public static EmptyMapBindingM forKeyValueTypes(final Class<?> keyType,
      final TypeLiteral<?> valueType) {
    return new EmptyMapBindingM(TypeLiteral.get(keyType), valueType, null);
  }

  public static EmptyMapBindingM forKeyValueTypes(final TypeLiteral<?> keyType,
      final Class<?> valueType) {
    return new EmptyMapBindingM(keyType, TypeLiteral.get(valueType), null);
  }

  public static EmptyMapBindingM forKeyValueTypes(final Class<?> keyType,
      final Class<?> valueType) {
    return new EmptyMapBindingM(TypeLiteral.get(keyType), TypeLiteral.get(valueType), null);
  }

  public EmptyMapBindingM annotatedWith(Class<? extends Annotation> annotation) {
    return new EmptyMapBindingM(keyType, valueType, checkNotNull(annotation));
  }

  @Override
  protected void configure() {
    if (annotation != null) {
      MapBinder.newMapBinder(binder(), keyType, valueType, annotation);
    } else {
      MapBinder.newMapBinder(binder(), keyType, valueType);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyType, valueType, annotation);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final EmptyMapBindingM other = (EmptyMapBindingM) obj;
    return Objects.equals(this.keyType, other.keyType)
        && Objects.equals(this.valueType, other.valueType)
        && Objects.equals(this.annotation, other.annotation);
  }
}
