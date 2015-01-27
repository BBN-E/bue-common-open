package com.bbn.bue.common.converters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts from a {@code String} to an instance of an arbitrary type. This conversion is done by
 * finding a constructor of the target type that takes a single {@code String} parameter and
 * invoking it.
 */
public class StringToClassInstance<T> implements StringConverter<T> {

  final Class<? extends T> type;
  final Constructor<? extends T> constructor;

  /**
   * Constructs a new converter for the given type.
   *
   * @param type the class to construct instances of
   * @throws IllegalArgumentException if {@code type} is abstract or doesn't have an accessible
   *                                  constructor that takes a single {@code String} argument. A
   *                                  constructor might not be accessible because its visibility
   *                                  doesn't allow this class to access it, or a security manager
   *                                  restricts access to {@code T}.
   */
  public StringToClassInstance(final Class<? extends T> type) {
    this.type = type;
    checkArgument(!type.isInterface(),
        "Type " + type + " is an interface and cannot be directly instantiated");
    checkArgument(!Modifier.isAbstract(type.getModifiers()),
        "Type " + type + " is abstract and cannot be directly instantiated");
    try {
      this.constructor = type.getConstructor(String.class);
    } catch (final NoSuchMethodException nsme) {
      throw new IllegalArgumentException("Cannot find a single-string constructor for type " + type,
          nsme);
    } catch (final SecurityException se) {
      throw new IllegalArgumentException(
          "Cannot access a single-string constructor for type " + type, se);
    }
  }

  /**
   * Constructs a new instance of {@code T} by invoking its single-string constructor.
   *
   * @param s the string to convert from
   * @return a new instance of {@code T}, instantiated as though {@code new T(s)} had been invoked
   * @throws ConversionException  if the constructor isn't accessible, {@code T} isn't a concrete
   *                              type, an exception is thrown by {@code T}'s constructor, or an
   *                              exception is thrown when loading {@code T} or one of its
   *                              dependency classes
   * @throws NullPointerException if {@code s} is null
   */
  @Override
  public T decode(final String s) {
    checkNotNull(s);
    try {
      return constructor.newInstance(s);
    } catch (final IllegalAccessException iae) {
      throw new ConversionException("Cannot access a single-string constructor for type " + type,
          iae);
    } catch (final InstantiationException ie) {
      throw new ConversionException(
          "Cannot instantiate " + type + ", probably because it is not concrete", ie);
    } catch (final InvocationTargetException ite) {
      throw new ConversionException("Exception thrown in constructor for " + type, ite);
    } catch (final ExceptionInInitializerError eiie) {
      throw new ConversionException(
          "Exception thrown when initializing classes for construction of " + type, eiie);
    }
  }
}
