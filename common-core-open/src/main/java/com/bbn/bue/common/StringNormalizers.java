package com.bbn.bue.common;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer2;

import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides various normalizers for strings
 *
 * @author Ryan Gabbard, Jay DeYoung
 */
public final class StringNormalizers {

  private StringNormalizers() {
    throw new UnsupportedOperationException();
  }

  /**
   * A {@code StringNormalizer} which does nothing.
   */
  public static StringNormalizer identity() {
    return IdentityNormalizer.INSTANCE;
  }

  /**
   * Applies the NFKC Unicode normalization to the given String.  You should probably do this or
   * {@link #toNfc()} on any strings from multiple sources being compared in all multilingual code
   * to deal with e.g. differences in composing accents. In particular, you probably want to apply
   * this before applying any other normalizers.
   *
   * See Unicode Standard Annex #15 section 1.2 http://www.unicode.org/reports/tr15/
   */
  public static StringNormalizer toNfkc() {
    return NfkcNormalizer.INSTANCE;
  }

  /**
   * Applies the NFC Unicode normalization to the given String.  You should probably do either this
   * or {@link #toNfkc()} on any strings from multiple sources being compared in all multilingual
   * code to deal with e.g. differences in composing accents. In particular, you probably want to
   * apply this before applying any other normalizers.
   *
   * See Unicode Standard Annex #15 section 1.2 http://www.unicode.org/reports/tr15/
   */
  public static StringNormalizer toNfc() {
    return NfcNormalizer.INSTANCE;
  }

  /**
   * A {@link StringNormalizer} which maps all Unicode codepoints matched by the provided {@link
   * CodepointMatcher} to the specified {@code replacementCharacter}.
   */
  public static StringNormalizer translate(CodepointMatcher codepointMatcher,
      char replacementCharacter) {
    return CodepointTranslatorStringNormalizer.of(codepointMatcher, replacementCharacter);
  }

  /**
   * A {@link StringNormalizer} which collapses all consecutive characters which match the specified
   * {@link CodepointMatcher} to the first such character.
   */
  public static StringNormalizer collapseConsecutive(CodepointMatcher codepointMatcher) {
    return CollapseConsecutiveCharacters.of(codepointMatcher);
  }

  @UnicodeUnsafe
  @EvalHack(eval = "LORELEI-Y2")
  public static StringNormalizer stripFromEnd(CharMatcher toRemove) {
    return StripFromEnd.of(toRemove);
  }

  /**
   * Converts the input to lower-case in a locale-sensitive way. Locales matter - for example the
   * lower case verison of I in Turkish is not i but dot-less i!
   */
  public static StringNormalizer toLowercase(SerifLocale locale) {
    return ToLowerCase.forLocale(locale);
  }

  /**
   * Converts the input to upper-case in a locale-sensitive way. Locales matter - for example the
   * upper case verison of i in Turkish is not I but İ!
   */
  public static StringNormalizer toUppercase(SerifLocale locale) {
    return ToUpperCase.forLocale(locale);
  }

  /**
   * A {@link StringNormalizer} which composes a sequence of {@link StringNormalizer}s.  The first
   * {@code StringNormalizer} in the provided sequence it applied first, then the second is
   * applied to its output, and so on.
   */
  public static StringNormalizer compose(Iterable<? extends StringNormalizer> stringNormalizers) {
    return CompositeStringNormalizer.of(stringNormalizers);
  }

  /**
   * A {@link StringNormalizer} which composes a sequence of {@link StringNormalizer}s.
   */
  public static StringNormalizer compose(StringNormalizer norm1, StringNormalizer... others) {
    final List<StringNormalizer> asList = new ArrayList<>();
    asList.add(norm1);
    asList.addAll(Arrays.asList(others));
    return CompositeStringNormalizer.of(asList);
  }

  public static Function<String, String> asFunction(StringNormalizer normalizer) {
    return NormalizerAsFunction.of(normalizer);
  }
}

enum IdentityNormalizer implements StringNormalizer {
  INSTANCE;

  @Override
  public String normalize(final String input) {
    return input;
  }

  @Override
  public String toString() {
    return "Identity";
  }
}

/**
 * See {@link StringNormalizers#translate(CodepointMatcher, char)}
 */
@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class CodepointTranslatorStringNormalizer implements StringNormalizer {

  @Value.Parameter
  public abstract CodepointMatcher codepointMatcher();

  @Value.Parameter
  public abstract char replacementCharacter();

  @Override
  public String normalize(final String input) {
    return codepointMatcher().replaceAll(input, replacementCharacter());
  }

  public static CodepointTranslatorStringNormalizer of(CodepointMatcher matcher,
      char replacementCharacter) {
    return ImmutableCodepointTranslatorStringNormalizer.of(matcher, replacementCharacter);
  }
}

/**
 * See {@link StringNormalizers#collapseConsecutive(CodepointMatcher)}
 */
@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class CollapseConsecutiveCharacters implements StringNormalizer {

  @Value.Parameter
  public abstract CodepointMatcher toCollapseMatcher();

  @Override
  public String normalize(final String input) {
    final StringBuilder ret = new StringBuilder();
    // -1 is out-of-range for Unicode
    int lastCodePoint = -1;
    for (int offset = 0; offset < input.length(); ) {
      final int curCodePoint = input.codePointAt(offset);
      final boolean curCashpointIsCollapsible = toCollapseMatcher().matches(curCodePoint);
      if (!curCashpointIsCollapsible || curCodePoint != lastCodePoint) {
        ret.append(Character.toChars(curCodePoint));
      }
      lastCodePoint = curCodePoint;
      offset += Character.charCount(curCodePoint);
    }
    return ret.toString();
  }

  public static CollapseConsecutiveCharacters of(CodepointMatcher matcher) {
    return ImmutableCollapseConsecutiveCharacters.of(matcher);
  }
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class StripFromEnd implements StringNormalizer {

  @Value.Parameter
  public abstract CharMatcher toStripMatcher();

  @Override
  @UnicodeUnsafe
  public String normalize(final String input) {
    return toStripMatcher().trimTrailingFrom(input);
  }

  public static StripFromEnd of(CharMatcher matcher) {
    return ImmutableStripFromEnd.of(matcher);
  }
}


/**
 * See {@link StringNormalizers#compose(Iterable)}
 */
@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class CompositeStringNormalizer implements StringNormalizer {

  /**
   * The {@link StringNormalizer}s to apply. The first in the list will be applied first; the last
   * in the list will be applied last. Each {@link StringNormalizer} is applied to the output of the
   * previous one.
   */
  @Value.Parameter
  public abstract ImmutableList<StringNormalizer> wordShapers();

  @Override
  public String normalize(final String input) {
    String cur = input;
    for (final StringNormalizer shaper : wordShapers()) {
      cur = shaper.normalize(cur);
    }
    return cur;
  }

  public static CompositeStringNormalizer of(Iterable<? extends StringNormalizer> wordShapers) {
    return ImmutableCompositeStringNormalizer.of(wordShapers);
  }
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class ToLowerCase implements StringNormalizer {

  @Value.Parameter
  public abstract SerifLocale locale();

  @Override
  public String normalize(final String input) {
    return UCharacter.toLowerCase(locale().asIcuLocale(), input);
  }

  public static ToLowerCase forLocale(SerifLocale locale) {
    return ImmutableToLowerCase.of(locale);
  }
}

@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class ToUpperCase implements StringNormalizer {

  @Value.Parameter
  public abstract SerifLocale locale();

  @Override
  public String normalize(final String input) {
    return UCharacter.toUpperCase(locale().asIcuLocale(), input);
  }

  public static ToUpperCase forLocale(SerifLocale locale) {
    return ImmutableToUpperCase.of(locale);
  }
}


/**
 * See {@link StringNormalizers#toNfkc()}
 */
enum NfkcNormalizer implements StringNormalizer {
  INSTANCE;

  private static final Normalizer2 icuNormalizer = Normalizer2.getNFKCInstance();

  @Override
  public String normalize(final String input) {
    return icuNormalizer.normalize(input);
  }

  @Override
  public String toString() {
    return "toNFKC()";
  }
}


/**
 * See {@link StringNormalizers#toNfc()}
 */
enum NfcNormalizer implements StringNormalizer {
  INSTANCE;

  private static final Normalizer2 icuNormalizer = Normalizer2.getNFCInstance();

  @Override
  public String normalize(final String input) {
    return icuNormalizer.normalize(input);
  }

  @Override
  public String toString() {
    return "toNFC()";
  }
}

/**
 * See {@link StringNormalizers#asFunction(StringNormalizer)}.
 */
@TextGroupImmutable
@Value.Immutable
@JsonSerialize
@JsonDeserialize
abstract class NormalizerAsFunction implements Function<String, String> {

  public static NormalizerAsFunction of(StringNormalizer normalizer) {
    return ImmutableNormalizerAsFunction.of(normalizer);
  }

  @Value.Parameter
  public abstract StringNormalizer stringNormalizer();

  @Override
  public final String apply(String s) {
    return stringNormalizer().normalize(s);
  }
}
