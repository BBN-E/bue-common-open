package com.bbn.bue.common;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

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
  public final int hashCode() {
    return utf16CodeUnits().hashCode();
  }

  @Override
  public final boolean equals(Object other) {
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
}
