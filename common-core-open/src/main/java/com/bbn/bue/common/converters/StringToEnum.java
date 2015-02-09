package com.bbn.bue.common.converters;

import com.google.common.base.Joiner;

import java.util.EnumSet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Converts from a {@code String} to an enum value. This conversion is done by invoking {@link
 * Enum#valueOf(Class, String)}.
 */
public class StringToEnum<T extends Enum<T>> implements StringConverter<T> {

  final Class<T> type;

  /**
   * Constructs a new converter.
   *
   * @param type the type of the enum to decode to
   * @throws IllegalArgumentException if {@code type} is not an enum class
   */
  public StringToEnum(final Class<T> type) {
    // Someone could have cast away our generic type constraint,
    // so we double-check that type really is an enum
    checkArgument(type.isEnum(), "Class " + type + " is not an enum");
    this.type = type;
  }

  /**
   * Converts a string to a value of the enum type. If the string cannot be converted to an enum
   * value as-is, conversion is attempted on the string in uppercase and then in lowercase as a
   * fallback.
   *
   * @param s the string to convert
   * @return an enum value
   * @throws NullPointerException if {@code s} is null
   * @throws ConversionException  if {@code s} cannot be converted to an enum value after attempting
   *                              fallback transformations
   */
  @Override
  public T decode(final String s) {
    checkNotNull(s);
    try {
      return Enum.valueOf(type, s);
    } catch (final IllegalArgumentException ignored) {
    }
    try {
      return Enum.valueOf(type, s.toUpperCase());
    } catch (final IllegalArgumentException ignored) {
    }
    try {
      return Enum.valueOf(type, s.toLowerCase());
    } catch (final IllegalArgumentException ignored) {
    }

    throw new ConversionException(
        "Unable to instantiate a " + type + " from value " + s + ". Valid values: " + Joiner
            .on(", ").join(EnumSet.allOf(type)));
  }
}
