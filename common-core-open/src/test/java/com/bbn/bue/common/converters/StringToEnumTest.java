package com.bbn.bue.common.converters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringToEnumTest {

  public static enum TotallyAnEnum {
    VALUE_1, VALUE_2, value_3
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings(value = {"unchecked", "rawtypes"})
  public void constructionOnNonEnumTypeThrows() {
    @SuppressWarnings("unused")
    final StringToEnum converter = new StringToEnum(Object.class);
  }

  @Test
  public void constructionOnEnumType() {
    @SuppressWarnings("unused")
    final StringToEnum<TotallyAnEnum> converter =
        new StringToEnum<TotallyAnEnum>(TotallyAnEnum.class);
  }

  @Test(expected = NullPointerException.class)
  public void decodingNullThrows() {
    final StringToEnum<TotallyAnEnum> converter =
        new StringToEnum<TotallyAnEnum>(TotallyAnEnum.class);
    converter.decode(null);
  }

  @Test
  public void basicDecoding() {
    final StringToEnum<TotallyAnEnum> converter =
        new StringToEnum<TotallyAnEnum>(TotallyAnEnum.class);
    for (final String s : new String[]{"VALUE_1", "VALUE_2", "value_3"}) {
      assertEquals(s, converter.decode(s).toString());
    }
  }

  @Test
  public void fallbackDecoding() {
    final StringToEnum<TotallyAnEnum> converter =
        new StringToEnum<TotallyAnEnum>(TotallyAnEnum.class);
    for (final String s : new String[]{"value_1", "VaLuE_1"}) {
      assertEquals("VALUE_1", converter.decode(s).toString());
    }

    for (final String s : new String[]{"value_2", "vALUE_2"}) {
      assertEquals("VALUE_2", converter.decode(s).toString());
    }

    for (final String s : new String[]{"VALUE_3", "vAlUE_3"}) {
      assertEquals("value_3", converter.decode(s).toString());
    }
  }

  @Test(expected = ConversionException.class)
  public void decodingInvalidValueThrows() {
    final StringToEnum<TotallyAnEnum> converter =
        new StringToEnum<TotallyAnEnum>(TotallyAnEnum.class);
    converter.decode("blug");
  }
}
