package com.bbn.bue.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class UnicodeFriendlyStringBuilder {
  private final StringBuilder sb;
  private int length;

  public UnicodeFriendlyStringBuilder(StringBuilder sb) {
    this.sb = checkNotNull(sb);
    this.length = sb.length();
  }


  public static UnicodeFriendlyStringBuilder create() {
    return new UnicodeFriendlyStringBuilder(new StringBuilder());
  }

  public static UnicodeFriendlyStringBuilder createWithCodeUnitCapacity(int capacity) {
    checkArgument(capacity >= 0);
    return new UnicodeFriendlyStringBuilder(new StringBuilder(capacity));
  }

  public static UnicodeFriendlyStringBuilder forInitialString(CharSequence seq) {
    return new UnicodeFriendlyStringBuilder(new StringBuilder(seq));
  }

  public static UnicodeFriendlyStringBuilder forInitialString(UnicodeFriendlyString ufs) {
    return new UnicodeFriendlyStringBuilder(new StringBuilder(ufs.utf16CodeUnits()));
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
    sb.reverse();
    return this;
  }

  public int lengthInCodepoints() {
    return length;
  }

  public UnicodeFriendlyString build() {
    // this could be made more efficient by not having to re-scan for non-BMP characters
    return StringUtils.unicodeFriendly(sb.toString());
  }
}
