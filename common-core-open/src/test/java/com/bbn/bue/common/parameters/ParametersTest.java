package com.bbn.bue.common.parameters;

import com.bbn.bue.common.parameters.exceptions.ParameterConversionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * Tests some of {@link Parameters}. As these tests were developed long after the fact, they do not have full coverage.
 */
public final class ParametersTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testStringList() {
    assertEquals(
        ImmutableList.of(),
        Parameters.fromMap(ImmutableMap.of("list", "")).getStringList("list"));
    assertEquals(
        ImmutableList.of("a"),
        Parameters.fromMap(ImmutableMap.of("list", "a")).getStringList("list"));
    assertEquals(
        ImmutableList.of("a", "b"),
        Parameters.fromMap(ImmutableMap.of("list", "a,b")).getStringList("list"));
  }

  @Test
  public void testIntegerList() {
    assertEquals(
        ImmutableList.of(1),
        Parameters.fromMap(ImmutableMap.of("list", "1")).getIntegerList("list"));
    assertEquals(
        ImmutableList.of(1, 2),
        Parameters.fromMap(ImmutableMap.of("list", "1,2")).getIntegerList("list"));
    exception.expect(ParameterConversionException.class);
    Parameters.fromMap(ImmutableMap.of("list", "a,b")).getIntegerList("list");
  }

  @Test
  public void testBooleanList() {
    assertEquals(
        ImmutableList.of(true),
        Parameters.fromMap(ImmutableMap.of("list", "true")).getBooleanList("list"));
    assertEquals(
        ImmutableList.of(true, false),
        Parameters.fromMap(ImmutableMap.of("list", "true,false")).getBooleanList("list"));
    exception.expect(ParameterConversionException.class);
    Parameters.fromMap(ImmutableMap.of("list", "a,b")).getBooleanList("list");
  }
}
