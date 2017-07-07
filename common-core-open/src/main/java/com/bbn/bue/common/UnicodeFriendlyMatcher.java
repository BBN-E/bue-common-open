package com.bbn.bue.common;

import com.bbn.bue.common.annotations.MoveToBUECommon;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.strings.offsets.UTF16Offset;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by rgabbard on 7/7/17.
 */
@MoveToBUECommon
public final class UnicodeFriendlyMatcher {

  private final Matcher wrappedMatcher;
  private final UnicodeFriendlyString matchedString;

  private UnicodeFriendlyMatcher(final Matcher wrappedMatcher,
      UnicodeFriendlyString matchedString) {
    this.wrappedMatcher = checkNotNull(wrappedMatcher);
    this.matchedString = checkNotNull(matchedString);
  }

  public static UnicodeFriendlyMatcher match(Pattern pattern, UnicodeFriendlyString ufs) {
    return new UnicodeFriendlyMatcher(pattern.matcher(ufs.utf16CodeUnits()), ufs);
  }

  public boolean find() {
    return wrappedMatcher.find();
  }

  public boolean matches() {
    return wrappedMatcher.matches();
  }

  public OffsetRange<CharOffset> matchOffsetsInclusive() {
    return OffsetRange.fromInclusiveEndpoints(start(), endExclusive().shiftedCopy(-1));
  }

  public OffsetRange<CharOffset> matchOffsetsInclusive(int group) {
    return OffsetRange.fromInclusiveEndpoints(start(group), endExclusive(group).shiftedCopy(-1));
  }

  public CharOffset start() {
    return startMatchedCodepointForCodeUnit(wrappedMatcher.start());
  }

  public CharOffset start(int group) {
    return startMatchedCodepointForCodeUnit(wrappedMatcher.start(group));
  }


  public CharOffset endExclusive() {
    return endMatchedCodepointForCodeUnit(wrappedMatcher.end());
  }

  public CharOffset endExclusive(int group) {
    return endMatchedCodepointForCodeUnit(wrappedMatcher.end(group));
  }

  public UnicodeFriendlyString group(int groupIndex) {
    return matchedString.substringByCodePoints(matchOffsetsInclusive(groupIndex));
  }

  public int groupCount() {
    return wrappedMatcher.groupCount();
  }

  private CharOffset startMatchedCodepointForCodeUnit(int codeunit) {
    return matchedString.codepointIndex(UTF16Offset.of(codeunit));
  }

  private CharOffset endMatchedCodepointForCodeUnit(int codeunit) {
    // the inner shift is necessary because wrappedMatcher.end() returns the index *past* the
    // end of the match. Since this might be out-of-bounds for the string, we might be unable to
    // map it to a code point offset. So we instead map the preceding character and then shift
    // the resulting codepoint by one to return an exclusive end offset
    return matchedString.codepointIndex(UTF16Offset.of(codeunit - 1)).shiftedCopy(1);
  }

}
