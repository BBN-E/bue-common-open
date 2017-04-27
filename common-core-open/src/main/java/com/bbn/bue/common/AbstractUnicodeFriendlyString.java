package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.base.Optional;

/**
 * Ensures certain common behaviors for {@link UnicodeFriendlyString} implementations, especially
 * that equality and hashcode are done by the UTF-16 code units (that is, the underlying Java
 * {@code String}).
 */
abstract class AbstractUnicodeFriendlyString implements UnicodeFriendlyString {

  public final UnicodeFriendlyString substringByCodePoints(OffsetRange<CharOffset> codePointRange) {
    return substringByCodePoints(codePointRange.startInclusive(),
        // version with separate arguments has exclusive endpoint
        codePointRange.endInclusive().shiftedCopy(1));
  }

  public final boolean contains(String otherCodeUnits) {
    return utf16CodeUnits().contains(otherCodeUnits);
  }

  public final boolean contains(UnicodeFriendlyString other) {
    return utf16CodeUnits().contains(other.utf16CodeUnits());
  }

  @Override
  public final boolean startsWith(final UnicodeFriendlyString ufs) {
    return startsWith(ufs, CharOffset.asCharOffset(0));
  }

  @Override
  public boolean startsWith(UnicodeFriendlyString ufs, CharOffset offset) {
    // simple but slow implementation
    final Optional<CharOffset> ret = codePointIndexOf(ufs, offset);
    return ret.isPresent() && ret.get().asInt() == 0;
  }

  @Override
  public int hashCode() {
    return utf16CodeUnits().hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    return other instanceof UnicodeFriendlyString && utf16CodeUnits()
        .equals(((UnicodeFriendlyString) other).utf16CodeUnits());
  }

  @Override
  public String toString() {
    return utf16CodeUnits();
  }

  @Override
  public final Optional<CharOffset> codePointIndexOf(UnicodeFriendlyString other) {
    return codePointIndexOf(other, CharOffset.asCharOffset(0));
  }

}
