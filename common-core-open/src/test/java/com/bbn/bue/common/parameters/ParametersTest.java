package com.bbn.bue.common.parameters;

import com.bbn.bue.common.parameters.exceptions.ParameterConversionException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.bbn.bue.common.parameters.Parameters.builder;
import static com.bbn.bue.common.parameters.Parameters.fromMap;
import static org.junit.Assert.assertEquals;

/**
 * Tests some of {@link Parameters}. As these tests were developed long after the fact, they do not have full coverage.
 */
public final class ParametersTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void testFromMap() {
    final ImmutableMap<String, String> map = ImmutableMap.of("a", "1", "b", "", "c.d", "2");
    // Basic parameter
    assertEquals("1", fromMap(map).getString("a"));
    // Empty parameter
    assertEquals("", fromMap(map).getString("b"));
    // Namespace specified by list
    assertEquals("", fromMap(map, ImmutableList.of("")).namespace());
    assertEquals(ImmutableList.of(), fromMap(map, ImmutableList.<String>of()).namespaceAsList());
    assertEquals("foo", fromMap(map, ImmutableList.of("foo")).namespace());
    assertEquals(ImmutableList.of("foo"), fromMap(map, ImmutableList.of("foo")).namespaceAsList());
    assertEquals("foo.bar", fromMap(map, ImmutableList.of("foo", "bar")).namespace());
    assertEquals(ImmutableList.of("foo", "bar"), fromMap(map, ImmutableList.of("foo", "bar")).namespaceAsList());
  }

  @Test
  public void testSplitNamespace() {
    assertEquals(ImmutableList.of(), Parameters.splitNamespace(""));
    assertEquals(ImmutableList.of("foo"), Parameters.splitNamespace("foo"));
    assertEquals(ImmutableList.of("foo", "bar"), Parameters.splitNamespace("foo.bar"));
  }

  @Test
  public void testJoinNamespace() {
    assertEquals("", Parameters.joinNamespace(ImmutableList.of("")));
    assertEquals("foo", Parameters.joinNamespace(ImmutableList.of("foo")));
    assertEquals("foo.bar", Parameters.joinNamespace(ImmutableList.of("foo", "bar")));
    assertEquals("foo.bar.baz", Parameters.joinNamespace(ImmutableList.of("foo.bar", "baz")));

    assertEquals("", Parameters.joinNamespace(""));
    assertEquals("foo", Parameters.joinNamespace("foo"));
    assertEquals("foo.bar", Parameters.joinNamespace("foo", "bar"));
    assertEquals("foo.bar.baz", Parameters.joinNamespace("foo.bar", "baz"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJoinNamespaceEndsWithPeriod() {
    Parameters.joinNamespace(ImmutableList.of("foo.bar.", "baz"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testJoinNamespaceStarsWithPeriod() {
    Parameters.joinNamespace(ImmutableList.of("foo.bar", ".baz"));
  }

  @Test
  public void testBuilder() {
    final ImmutableMap<String, String> map = ImmutableMap.of("a", "1", "b", "2");
    // Empty namespace
    assertEquals("1", builder().putAll(map).build().getString("a"));
    assertEquals("1", fromMap(map).modifiedCopyBuilder().build().getString("a"));
    // With namespace
    assertEquals("1", builder(ImmutableList.of("foo")).putAll(map).build().getString("a"));
    assertEquals("1", fromMap(map, ImmutableList.of("foo")).modifiedCopyBuilder().build().getString("a"));
    // Check namespace
    assertEquals("", builder().putAll(map).build().namespace());
    assertEquals("", fromMap(map).modifiedCopyBuilder().build().namespace());
    assertEquals("foo", builder(ImmutableList.of("foo")).putAll(map).build().namespace());
    assertEquals("foo", fromMap(map, ImmutableList.of("foo")).modifiedCopyBuilder().build().namespace());
    assertEquals("foo.bar", builder(ImmutableList.of("foo", "bar")).putAll(map).build().namespace());
    assertEquals("foo.bar", fromMap(map, ImmutableList.of("foo", "bar")).modifiedCopyBuilder().build().namespace());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderEmptyKey() {
    final Parameters.Builder builder = Parameters.builder();
    builder.set("", "bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderWhitespaceKey() {
    final Parameters.Builder builder = Parameters.builder();
    builder.set("fo o", "bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderOnlyWhitespaceKey() {
    final Parameters.Builder builder = Parameters.builder();
    builder.set(" ", "bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderEmptyValue() {
    final Parameters.Builder builder = Parameters.builder();
    builder.set("foo", "");
  }

  @Test
  public void testBuilderWhitespaceValue() {
    final Parameters.Builder builder = Parameters.builder();
    builder.set("foo", " ba r ");
    // Leading and trailing whitespace should be trimmed
    assertEquals("ba r", builder.build().getString("foo"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBuilderOnlyWhitespaceValue() {
    final Parameters.Builder builder = Parameters.builder();
    builder.set("foo", " ");
  }

  @Test
  public void testStringList() {
    assertEquals(
        ImmutableList.of(),
        fromMap(ImmutableMap.of("list", "")).getStringList("list"));
    assertEquals(
        ImmutableList.of("a"),
        fromMap(ImmutableMap.of("list", "a")).getStringList("list"));
    assertEquals(
        ImmutableList.of("a", "b"),
        fromMap(ImmutableMap.of("list", "a,b")).getStringList("list"));
  }

  @Test
  public void testIntegerList() {
    assertEquals(
        ImmutableList.of(1),
        fromMap(ImmutableMap.of("list", "1")).getIntegerList("list"));
    assertEquals(
        ImmutableList.of(1, 2),
        fromMap(ImmutableMap.of("list", "1,2")).getIntegerList("list"));
    exception.expect(ParameterConversionException.class);
    fromMap(ImmutableMap.of("list", "a,b")).getIntegerList("list");
  }

  @Test
  public void testBooleanList() {
    assertEquals(
        ImmutableList.of(true),
        fromMap(ImmutableMap.of("list", "true")).getBooleanList("list"));
    assertEquals(
        ImmutableList.of(true, false),
        fromMap(ImmutableMap.of("list", "true,false")).getBooleanList("list"));
    exception.expect(ParameterConversionException.class);
    fromMap(ImmutableMap.of("list", "a,b")).getBooleanList("list");
  }
}
