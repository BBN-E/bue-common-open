package com.bbn.bue.common;

import org.junit.Test;

import static com.bbn.bue.common.CodepointMatcher.digit;
import static com.bbn.bue.common.CodepointMatcher.not;
import static junit.framework.TestCase.assertEquals;

public class CodePointMatcherTest {

  final String CHEESE_WEDGE = "\uD83E\uDDC0";

  @Test
  public void testReplaceAll() {
    final String hasNonBmpAlphabetic = "ab\uD87E\uDDF3de123fg";
    final String reference = "AAAAA123AA";
    final String predicted = CodepointMatcher.alphabetic().replaceAll(hasNonBmpAlphabetic, 'A');
    assertEquals(reference, predicted);
  }

  @Test
  public void testNegation() {
    assertEquals(2, not(digit()).countIn("14bC56"));
  }

  @Test
  public void testCollapseFrom() {
    final String toCollapse =
        "a" + CHEESE_WEDGE + "aabbc\uD87E\uDDF3cabc" + CHEESE_WEDGE + "ccaacaba";
    final String reference = "-c\uD87E\uDDF3c-c-cc-c-";
    final String predicted =
        CodepointMatcher.anyOf("a" + CHEESE_WEDGE + "b").collapseFrom(toCollapse,
            '-');
    assertEquals(reference, predicted);
    final String toCollapse2 = "a" + CHEESE_WEDGE + "aabbab" + CHEESE_WEDGE + "aaaba";
    final String reference2 = "-";
    final String predicted2 =
        CodepointMatcher.anyOf("a" + CHEESE_WEDGE + "b").collapseFrom(toCollapse2,
            '-');
    assertEquals(reference2, predicted2);
    final String toCollapse3 =
        "a" + CHEESE_WEDGE + "aabbc\uD87E\uDDF3cabc" + CHEESE_WEDGE + "ccaacaba";
    final String reference3 =
        "a" + CHEESE_WEDGE + "aabbc\uD87E\uDDF3cabc" + CHEESE_WEDGE + "ccaacaba";
    final String predicted3 = CodepointMatcher.anyOf("7").collapseFrom(toCollapse,
        '-');
    assertEquals(reference3, predicted3);
  }

  @Test
  public void testTrimFrom() {
    final String toTrim = "a\uD87E\uDDF3a" + CHEESE_WEDGE + "aba" + CHEESE_WEDGE + "abaa";
    final String reference = "\uD87E\uDDF3a" + CHEESE_WEDGE + "aba" + CHEESE_WEDGE + "ab";
    final String predicted = CodepointMatcher.anyOf("a" + CHEESE_WEDGE).trimFrom(toTrim);
    assertEquals(reference, predicted);
    final String toTrim2 = "aa" + CHEESE_WEDGE + "aa";
    final String reference2 = "";
    final String predicted2 = CodepointMatcher.anyOf("a" + CHEESE_WEDGE).trimFrom(toTrim2);
    assertEquals(reference2, predicted2);
    final String toTrim3 = "a\uD87E\uDDF3aabaabaa";
    final String reference3 = "a\uD87E\uDDF3aabaabaa";
    final String predicted3 = CodepointMatcher.is(CHEESE_WEDGE).trimFrom(toTrim3);
    assertEquals(reference3, predicted3);
  }

  @Test
  public void testRemoveFrom() {
    final String toRemove = "abc\uD87E\uDDF3" + CHEESE_WEDGE + "abcabcdd" + CHEESE_WEDGE + "abc";
    final String reference = "c\uD87E\uDDF3ccddc";
    final String predicted = CodepointMatcher.anyOf(CHEESE_WEDGE + "ab").removeFrom(toRemove);
    assertEquals(reference, predicted);
    final String toRemove2 = "abc" + CHEESE_WEDGE + "abcabcdd" + CHEESE_WEDGE + "abc";
    final String reference2 = "";
    final String predicted2 = CodepointMatcher.anyOf(CHEESE_WEDGE + "abcd").removeFrom(toRemove2);
    assertEquals(reference2, predicted2);
  }

  @Test
  public void testTrimAndCollapseFrom() {
    final String toTrimAndCollapse = "abcabcabcddabcaaaa";
    final String reference = "bc-bc-bcdd-bc";
    final String predicted = CodepointMatcher.is("a").trimAndCollapseFrom(toTrimAndCollapse, '-');
    assertEquals(reference, predicted);
    final String toTrimAndCollapse2 =
        "\uD87E\uDDF3bc\uD87E\uDDF3bc\uD87E\uDDF3bc" + CHEESE_WEDGE + "dd\uD87E\uDDF3bca"
            + "\uD87E\uDDF3\uD87E\uDDF3";
    final String reference2 = "bc-bc-bc" + CHEESE_WEDGE + "dd-bca";
    final String predicted2 = CodepointMatcher.is("\uD87E\uDDF3").
        trimAndCollapseFrom(toTrimAndCollapse2, '-');
    assertEquals(reference2, predicted2);
  }
}
