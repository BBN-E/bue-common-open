package com.bbn.bue.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CharMatcher;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Like Guava's {@link com.google.common.base.CharMatcher}, but handles codepoints outside the BMP.
 * Right now this is implemented rather inefficiently, but we can do fancy things with lookup
 * tables, etc. to speed things up in the future.
 *
 * This class is heavily indebted to Guava's {@code CharMatcher} for its design and part of its
 * implementation.
 *
 * Unlike {@code CharMatcher} this operates over {@link String}s rather than more generic {@link
 * CharSequence}s because in pre-8 Java you can't easily get the code points of a {@code
 * CharSequence}.
 *
 * Behavior in the presence of unpaired surrogates is undefined.
 *
 * @author Ryan Gabbard, Noah Rivkin, Jay DeYoung
 */
public abstract class CodepointMatcher implements Predicate<Integer> {

  protected CodepointMatcher() {
  }

  /**
   * Matches any character.
   */
  public static CodepointMatcher any() {
    return ANY;
  }

  /**
   * Matches no characters.
   */
  public static CodepointMatcher none() {
    return NONE;
  }

  /**
   * Matches any character in the sequence
   */
  public static CodepointMatcher anyOf(final String sequence) {
    switch (sequence.length()) {
      case 0:
        return none();
      case 1:
        return is(sequence);
      default:
        return new AnyOf(sequence);
    }
  }

  /**
   * Matches the character given as the argument
   */
  public static CodepointMatcher is(String s) {
    return new Is(s);
  }

  /**
   * Matches anything matched by {@link Character#isWhitespace(int)}
   */
  public static CodepointMatcher whitespace() {
    return Whitespace.INSTANCE;
  }

  /**
   * Matches codepoints in the Unicode categories: {@code CONNECTOR_PUNCTUATION, DASH_PUNCTUATION,
   * END_PUNCTUATION, FINAL_QUOTE_PUNCTUATION, INITIAL_QUOTE_PUNCTUATION, START_PUNCTUATION,
   * OTHER_PUNCTUATION.}
   */
  public static CodepointMatcher punctuation() {
    return Punctuation.INSTANCE;
  }

  /**
   * Matches code points in the Unicode general category {@link Character#CURRENCY_SYMBOL}.
   */
  public static CodepointMatcher currencySymbols() {
    return Currency.INSTANCE;
  }

  /**
   * Matches code points matched by {@link Character#isAlphabetic(int)}.
   */
  public static CodepointMatcher alphabetic() {
    return Alphabetic.INSTANCE;
  }

  /**
   * Matches codepoints matched by {@link Character#isLetter(int)}
   */
  public static CodepointMatcher letter() {
    return Letter.INSTANCE;
  }

  /**
   * Matches code points matched by {@link Character#isAlphabetic(int)} or {@link
   * Character#isDigit(int)}.
   */
  public static CodepointMatcher alphanumeric() {
    return Alphanumeric.INSTANCE;
  }

  /**
   * Matches code points matched by {@link Character#isUpperCase(char)}.
   */
  public static CodepointMatcher uppercase() {
    return Uppercase.INSTANCE;
  }

  /**
   * Matches code points matched by {@link Character#isLowerCase(char)} (char)}.
   */
  public static CodepointMatcher lowercase() {
    return Lowercase.INSTANCE;
  }

  /**
   * Matches code points matched with the Unicode category {@code TITLECASE_LETTER}.
   */
  public static CodepointMatcher titlecaseLetter() {
    return new HasUnicodeCategory(Character.TITLECASE_LETTER);
  }

  /**
   * Matches code points matched with the Unicode category {@code OTHER_LETTER}.
   */
  public static CodepointMatcher otherLetter() {
    return new HasUnicodeCategory(Character.OTHER_LETTER);
  }

  /**
   * Matches code points matched with the Unicode category {@code MODIFIER_LETTER}.
   */
  public static CodepointMatcher modifierLetter() {
    return new HasUnicodeCategory(Character.MODIFIER_LETTER);
  }

  /**
   * Matches codepoints which are neither upper, lower, nor title cased either by general category
   * or contributory properties.
   */
  public static CodepointMatcher uncasedLetter() {
    return UNCASED;
  }

  /**
   * Matches the code points matched by {@link Character#isDigit(char)}.
   */
  public static CodepointMatcher digit() {
    return Digit.INSTANCE;
  }

  /**
   * Get a {@link CodepointMatcher} which returns the opposite of the wrapped {@code
   * CodepointMatcher}.
   */
  public static CodepointMatcher not(CodepointMatcher wrapped) {
    return new Not(wrapped);
  }

  /**
   * Gets a {@link CodepointMatcher} which returns {@code true} iff either one of {@code left} or
   * {@code right} returns {@code true}
   */
  public static CodepointMatcher or(CodepointMatcher left, CodepointMatcher right) {
    return new Or(left, right);
  }

  /**
   * Gets a {@link CodepointMatcher} which returns {@code true} iff both of {@code left} and {@code
   * right} return {@code true}
   */
  public static CodepointMatcher and(CodepointMatcher left, CodepointMatcher right) {
    return new And(left, right);
  }

  public static CharMatcher asCharMatcher(final CodepointMatcher matcher) {
    return CharMatcher.forPredicate(new Predicate<Character>() {
      @Override
      public boolean apply(@Nullable final Character input) {
        return matcher.matches(input.charValue());
      }
    });
  }

  /**
   * @deprecated Prefer the typo-less {@link #basicMultilingualPlane()}
   */
  @Deprecated
  public static CodepointMatcher basicMultilingualPlace() {
    return basicMultilingualPlane();
  }

  /**
   * Matches all characters in the basic multilingual plane.
   */
  public static CodepointMatcher basicMultilingualPlane() {
    return BMP.INSTANCE;
  }

  public abstract boolean matches(int codepoint);

  public final boolean matchesNoneOf(String s) {
    return offsetIn(s) == NO_MATCH_OFFSET;
  }

  public final boolean matchesAnyOf(String s) {
    return !matchesNoneOf(s);
  }

  public final boolean matchesAllOf(String s) {
    // why this loop? Java stores strings as UTF-16, so logical characters may be one or two
    // chars. We need to be careful in case we get e.g. Asian language input
    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      if (!matches(codePoint)) {
        return false;
      }
      offset += Character.charCount(codePoint);
    }
    return true;
  }

  /**
   * Value returned by {@link #offsetIn(String)} when there is no match.
   */
  public static final int NO_MATCH_OFFSET = -1;

  /**
   * Returns the character offset (not the code point index!) in the provided string of the first
   * code point which is matched.  If there is no match, {@link #NO_MATCH_OFFSET} is returned.
   */
  public final int offsetIn(String s) {
    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      if (matches(codePoint)) {
        return offset;
      }
      offset += Character.charCount(codePoint);
    }
    return NO_MATCH_OFFSET;
  }

  public final int countIn(String s) {
    int count = 0;
    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      if (matches(codePoint)) {
        ++count;
      }
      offset += Character.charCount(codePoint);
    }
    return count;
  }

  /**
   * Returns a copy of the input string with all Unicode codepoints matching this matcher removed
   */
  public final String removeFrom(String s) {
    final StringBuilder sb = new StringBuilder();

    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      if (!matches(codePoint)) {
        sb.appendCodePoint(codePoint);
      }
      offset += Character.charCount(codePoint);
    }

    return sb.toString();
  }

  /**
   * Returns a copy of the input string with all leading and trailing codepoints matching this
   * matcher removed
   */
  public final String trimFrom(String s) {
    int first;
    int last;

    // removes leading matches
    for (first = 0; first < s.length(); ) {
      final int codePoint = s.codePointAt(first);
      if (!matches(codePoint)) {
        break;
      }
      first += Character.charCount(codePoint);
    }

    //remove trailing matches
    for (last = s.length() - 1; last >= first; --last) {
      if (Character.isLowSurrogate(s.charAt(last))) {
        --last;
      }
      if (!matches(s.codePointAt(last))) {
        break;
      }
    }

    return s.substring(first, last + 1);
  }

  /**
   * Returns a copy of the input string with all Unicode codepoints matching this matcher replaced
   * with {@code replacementCharacter}.
   */
  public final String replaceAll(String s, char replacementCharacter) {
    final StringBuilder sb = new StringBuilder();

    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      if (matches(codePoint)) {
        sb.append(replacementCharacter);
      } else {
        sb.appendCodePoint(codePoint);
      }
      offset += Character.charCount(codePoint);
    }

    return sb.toString();
  }

  /**
   * Returns a copy of the input string with all groups of 1 or more successive matching characters
   * are replaced with {@code replacementCharacter}.
   */
  public final String collapseFrom(String s, char replacementCharacter) {
    final StringBuilder sb = new StringBuilder();
    boolean follows = false;

    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      if (matches(codePoint)) {
        if (!follows) {
          sb.append(replacementCharacter);
        }
        follows = true;
      } else {
        sb.appendCodePoint(codePoint);
        follows = false;
      }
      offset += Character.charCount(codePoint);
    }

    return sb.toString();
  }

  /**
   * Returns a copy of the input string that has been trimmed, and then collapsed
   */
  public final String trimAndCollapseFrom(String s, char replacementCharacter) {
    return collapseFrom(trimFrom(s), replacementCharacter);
  }

  @Override
  public final boolean apply(final Integer codepoint) {
    return matches(codepoint);
  }

  public static CodepointMatcher forPredicate(Predicate<? super Integer> predicate) {
    return predicate instanceof CodepointMatcher ? (CodepointMatcher) predicate
                                                 : new ForPredicate(predicate);
  }

  public static CodepointMatcher forCharacter(char c) {
    // since c is single-byte in UTF-16, we can just do this cast
    return new IsCodePoint((int) c);
  }

  private static final class ForPredicate extends CodepointMatcher {

    private final Predicate<? super Integer> predicate;

    @JsonCreator
    ForPredicate(@JsonProperty("predicate") final Predicate<? super Integer> predicate) {
      this.predicate = checkNotNull(predicate);
    }

    @JsonProperty("predicate")
    Predicate<? super Integer> predicate() {
      return predicate;
    }

    @Override
    public boolean matches(final int codepoint) {
      return predicate.apply(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.forPredicate(" + predicate + ")";
    }
  }

  private static final class IsCodePoint extends CodepointMatcher {

    private final int codepoint;

    @JsonCreator
    public IsCodePoint(@JsonProperty("codepoint") final int codepoint) {
      this.codepoint = codepoint;
    }

    @JsonProperty("codepoint")
    int codepoint() {
      return codepoint;
    }

    @Override
    public boolean matches(final int codepoint) {
      return codepoint == this.codepoint;
    }

    @Override
    public String toString() {
      return new StringBuilder().append("CodepointMatcher.forCodepoint(")
          .appendCodePoint(codepoint).append(")").toString();
    }

    @Override
    public int hashCode() {
      return Objects.hash(codepoint);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final IsCodePoint other = (IsCodePoint) obj;
      return Objects.equals(this.codepoint, other.codepoint);
    }
  }

  private static final class HasUnicodeCategory extends CodepointMatcher {

    @JsonProperty("category")
    private final byte category;

    @JsonCreator
    HasUnicodeCategory(@JsonProperty("category") final byte category) {
      this.category = category;
    }

    @Override
    public boolean matches(final int codepoint) {
      return Character.getType(codepoint) == category;
    }

    @Override
    public String toString() {
      return new StringBuilder().append("CodepointMatcher.hasUnicodeCategory(")
          .append(category).append(")").toString();
    }

    @Override
    public int hashCode() {
      return Objects.hash(category);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final HasUnicodeCategory other = (HasUnicodeCategory) obj;
      return Objects.equals(this.category, other.category);
    }
  }

  private static final class Alphabetic extends CodepointMatcher {

    private static final Alphabetic INSTANCE = new Alphabetic();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isAlphabetic(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.alphabetic()";
    }
  }

  private static final class Letter extends CodepointMatcher {

    private static final Letter INSTANCE = new Letter();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isLetter(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.letter()";
    }
  }


  private static final class Alphanumeric extends CodepointMatcher {

    private static final Alphanumeric INSTANCE = new Alphanumeric();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isDigit(codepoint)
          || Character.isAlphabetic(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.alphanumeric()";
    }
  }

  private static final class Punctuation extends CodepointMatcher {

    private static final Punctuation INSTANCE = new Punctuation();

    @Override
    public boolean matches(final int codePoint) {
      final int category = Character.getType(codePoint);
      return category == Character.CONNECTOR_PUNCTUATION
          || category == Character.DASH_PUNCTUATION
          || category == Character.END_PUNCTUATION
          || category == Character.FINAL_QUOTE_PUNCTUATION
          || category == Character.INITIAL_QUOTE_PUNCTUATION
          || category == Character.START_PUNCTUATION
          || category == Character.OTHER_PUNCTUATION;
    }

    @Override
    public String toString() {
      return "CodepointMatcher.punctuation()";
    }
  }

  private static final class Currency extends CodepointMatcher {

    private static final Currency INSTANCE = new Currency();

    @Override
    public boolean matches(final int codePoint) {
      return Character.getType(codePoint) == Character.CURRENCY_SYMBOL;
    }

    @Override
    public String toString() {
      return "CodepointMatcher.currencySymbol()";
    }
  }

  private static final class Digit extends CodepointMatcher {

    private static final Digit INSTANCE = new Digit();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isDigit(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.digit()";
    }
  }

  private static final class Uppercase extends CodepointMatcher {

    private static final Uppercase INSTANCE = new Uppercase();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isUpperCase(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.uppercase()";
    }
  }

  private static final class Lowercase extends CodepointMatcher {

    private static final Lowercase INSTANCE = new Lowercase();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isLowerCase(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.lowercase()";
    }
  }

  private static final class BMP extends CodepointMatcher {

    private static final BMP INSTANCE = new BMP();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isBmpCodePoint(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.basicMultilingualPlane()";
    }
  }

  private static final class Whitespace extends CodepointMatcher {

    private static final Whitespace INSTANCE = new Whitespace();

    @Override
    public boolean matches(final int codepoint) {
      return Character.isWhitespace(codepoint);
    }

    @Override
    public String toString() {
      return "CodepointMatcher.whitespace()";
    }
  }

  private static final class Not extends CodepointMatcher {

    @JsonProperty("wrapped")
    private final CodepointMatcher wrapped;

    @JsonCreator
    Not(@JsonProperty("wrapped") final CodepointMatcher wrapped) {
      this.wrapped = checkNotNull(wrapped);
    }

    @Override
    public boolean matches(final int codepoint) {
      return !wrapped.matches(codepoint);
    }

    @Override
    public int hashCode() {
      return Objects.hash(wrapped);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Not other = (Not) obj;
      return Objects.equals(this.wrapped, other.wrapped);
    }

  }

  private static final class Or extends CodepointMatcher {

    @JsonProperty("left")
    private final CodepointMatcher left;

    @JsonProperty("right")
    private final CodepointMatcher right;

    Or(@JsonProperty("left") final CodepointMatcher left,
        @JsonProperty("right") final CodepointMatcher right) {
      this.left = checkNotNull(left);
      this.right = checkNotNull(right);
    }

    @Override
    public boolean matches(final int codepoint) {
      return left.matches(codepoint) || right.matches(codepoint);
    }

    @Override
    public int hashCode() {
      return Objects.hash(left, right);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Or other = (Or) obj;
      return Objects.equals(this.left, other.left)
          && Objects.equals(this.right, other.right);
    }
  }


  private static final class And extends CodepointMatcher {

    @JsonProperty("left")
    private final CodepointMatcher left;

    @JsonProperty("right")
    private final CodepointMatcher right;

    And(@JsonProperty("left") final CodepointMatcher left,
        @JsonProperty("right") final CodepointMatcher right) {
      this.left = checkNotNull(left);
      this.right = checkNotNull(right);
    }

    @Override
    public boolean matches(final int codepoint) {
      return left.matches(codepoint) && right.matches(codepoint);
    }

    @Override
    public int hashCode() {
      return Objects.hash(left, right);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final And other = (And) obj;
      return Objects.equals(this.left, other.left)
          && Objects.equals(this.right, other.right);
    }
  }

  private static final class AnyOf extends CodepointMatcher {

    final private String sequence;
    private int[] codepoints;

    public AnyOf(String sequence) {
      this.sequence = sequence;
      List<Integer> codepointsList = new ArrayList<>();
      final int length = sequence.length();
      int offset = 0;
      for (; offset < length; ) {
        final int codepoint = sequence.codePointAt(offset);
        codepointsList.add(codepoint);
        offset += Character.charCount(codepoint);
      }
      this.codepoints = new int[offset];
      // sorting the codepoints is somewhat expensive, but will pay off if for long sequences that
      // are used repeatedly
      codepoints = Ints.toArray(codepointsList);
      Arrays.sort(codepoints);
    }

    @Override
    public boolean matches(final int codepoint) {
      return Arrays.binarySearch(codepoints, codepoint) >= 0;
    }

    @Override
    public String toString() {
      return "CodepointMatcher.anyof(" + this.sequence + ")";
    }
  }

  private static final class Is extends CodepointMatcher {

    final private String s;
    final private int match;

    public Is(String s) {
      this.s = s;
      match = s.codePointAt(0);
      checkArgument(s.length() == Character.charCount(match));
    }

    @Override
    public boolean matches(final int codepoint) {
      return codepoint == match;
    }

    @Override
    public String toString() {
      return "CodepointMatcher.is(" + s + ")";
    }
  }


  private static final CodepointMatcher ANY =
      CodepointMatcher.forPredicate(Predicates.alwaysTrue());
  private static final CodepointMatcher NONE =
      CodepointMatcher.forPredicate(Predicates.alwaysFalse());

  private static final CodepointMatcher UNCASED =
      and(letter(),
          not(or(
              titlecaseLetter(),
              or(
                  uppercase(),
                  lowercase()))));
}
