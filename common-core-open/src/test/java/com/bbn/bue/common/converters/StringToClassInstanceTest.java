package com.bbn.bue.common.converters;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link StringToClassInstance}. Not all error conditions of the decoding method are tested
 * (in particular, there aren't test cases that very behavior when unusual exceptions are thrown
 * during constructor invocation).
 */
public class StringToClassInstanceTest {

  public static interface Interface {

    public String getValue();
  }

  public abstract static class AbstractClass implements Interface {

  }

  public static class NoStringConstructorClass implements Interface {

    @Override
    public String getValue() {
      return "value";
    }
  }

  public static class StringConstructorClass implements Interface {

    private final String value;

    public StringConstructorClass(final String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value;
    }
  }

  public static class PrivateStringConstructorClass implements Interface {

    private final String value;

    protected PrivateStringConstructorClass(final String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value;
    }
  }

  public static class ThrowingStringConstructorClass implements Interface {

    private final String value;

    public ThrowingStringConstructorClass(final String value) {
      throw new IllegalArgumentException();
    }

    @Override
    public String getValue() {
      return value;
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructionOnInterfaceThrows() {
    @SuppressWarnings("unused")
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<Interface>(Interface.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructionOnAbstractClassThrows() {
    @SuppressWarnings("unused")
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<AbstractClass>(AbstractClass.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructionOnNoSingleStringConstructorThrows() {
    @SuppressWarnings("unused")
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<NoStringConstructorClass>(NoStringConstructorClass.class);
  }

  @Test
  public void constructionOnSingleStringConstructor() {
    @SuppressWarnings("unused")
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<StringConstructorClass>(StringConstructorClass.class);
  }

  @Test(expected = NullPointerException.class)
  public void decodingNullThrows() {
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<StringConstructorClass>(StringConstructorClass.class);
    converter.decode(null);
  }

  @Test
  public void decodingPreservesValue() {
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<StringConstructorClass>(StringConstructorClass.class);
    for (final String s : new String[]{"foo", "bar", "", "baz", "quux"}) {
      assertEquals(s, converter.decode(s).getValue());
    }
  }

  @Test(expected = ConversionException.class)
  public void decodingThrowingConstructorThrows() {
    final StringToClassInstance<? extends Interface> converter =
        new StringToClassInstance<ThrowingStringConstructorClass>(
            ThrowingStringConstructorClass.class);
    converter.decode("foo");
  }

  // Construction of a class whose constructor can't be
  // called due to security or visibility modifiers

  // @Test(expected = ConversionException.class)
  // public void decodingInaccessibleConstructorThrows() {
  //     final StringToClassInstance<? extends Interface> converter = new StringToClassInstance<PrivateStringConstructorClass>(PrivateStringConstructorClass.class);
  //     converter.decode("foo");
  // }

  // Construction of a class that throws an exception when
  // initializing dependency classes

  // @Test(expected = ConversionException.class)
  // public void decodingThrowingClassThrows() {
  //     final StringToClassInstance<? extends Interface> converter = new StringToClassInstance<ClassLoadingThrowingStringConstructorClass>(ClassLoadingThrowingStringConstructorClass.class);
  //     converter.decode("foo");
  // }
}
