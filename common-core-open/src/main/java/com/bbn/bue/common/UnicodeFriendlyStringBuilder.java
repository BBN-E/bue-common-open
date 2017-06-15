package com.bbn.bue.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class UnicodeFriendlyStringBuilder {
  private final StringBuilder sb;
  private int length;

  // we guarantee length is correct in our static factory methods
  // and do not check for a match here
  private UnicodeFriendlyStringBuilder(StringBuilder sb, int length) {
    this.sb = checkNotNull(sb);
    this.length = length;
  }


  public static UnicodeFriendlyStringBuilder create() {
    return new UnicodeFriendlyStringBuilder(new StringBuilder(), 0);
  }

  public static UnicodeFriendlyStringBuilder createWithCodeUnitCapacity(int capacity) {
    checkArgument(capacity >= 0);
    return new UnicodeFriendlyStringBuilder(new StringBuilder(capacity), 0);
  }

  public static UnicodeFriendlyStringBuilder forInitialString(String s) {
    return new UnicodeFriendlyStringBuilder(new StringBuilder(s), StringUtils.codepointCount(s));
  }

  public static UnicodeFriendlyStringBuilder forInitialString(UnicodeFriendlyString ufs) {
    return new UnicodeFriendlyStringBuilder(new StringBuilder(ufs.utf16CodeUnits()),
        ufs.lengthInCodePoints());
  }

  public UnicodeFriendlyStringBuilder append(Object obj) {
    append(String.valueOf(obj));
    return this;
  }

  public UnicodeFriendlyStringBuilder append(String s) {
    // simple but relatively slow implementation
    return append(StringUtils.unicodeFriendly(s));
  }

  public UnicodeFriendlyStringBuilder append(UnicodeFriendlyString ufs) {
    length += ufs.lengthInCodePoints();
    sb.append(ufs.utf16CodeUnits());
    return this;
  }

  public UnicodeFriendlyStringBuilder appendCodePoint(int codePoint) {
    ++length;
    sb.appendCodePoint(codePoint);
    return this;
  }

  public UnicodeFriendlyStringBuilder reverse() {
    // StringBuilder#reverse's documentation guarantees it handles surrogate pairs correctly
    // so this is safe
    sb.reverse();
    return this;
  }

  public int lengthInCodepoints() {
    return length;
  }

  public boolean isEmpty() {
    return lengthInCodepoints() == 0;
  }


  /**
   * Clears the contents of this {@code UnicodeFriendlyStringBuilder}.  In the usual JDK
   * implementations this will maintain the underlying buffer so that future use of this string
   * builder should not require buffer re-allocation. However, this is not guaranteed.
   *
   * {@link StringBuilder} doesn't have a corresponding method, but it is a frequent pattern to
   * use the less clear {@code sb.setLenth(0)} to achieve the same thing.
   */
  public UnicodeFriendlyStringBuilder clearProbablyMaintainingBuffer() {
    sb.setLength(0);
    length = 0;
    return this;
  }

  public UnicodeFriendlyString build() {
    // this could be made more efficient by not having to re-scan for non-BMP characters
    return StringUtils.unicodeFriendly(sb.toString());
  }
}
