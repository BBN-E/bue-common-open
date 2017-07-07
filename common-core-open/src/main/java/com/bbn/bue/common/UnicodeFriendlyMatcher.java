package com.bbn.bue.common;

import com.bbn.bue.common.annotations.MoveToBUECommon;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.strings.offsets.UTF16Offset;

import com.google.common.base.Optional;

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

  public Optional<OffsetRange<CharOffset>> matchOffsetsInclusive(int group) {
    final boolean successfulMatchWithNoText =
        wrappedMatcher.start(group) == wrappedMatcher.end(group);

    if (successfulMatchWithNoText) {
      return Optional.absent();
    }

    final Optional<CharOffset> startGroupCodepoint = start(group);
    final Optional<CharOffset> endGroupCodepointExclusive = endExclusive(group);

    if (startGroupCodepoint.isPresent() && endGroupCodepointExclusive.isPresent()) {
      return Optional.of(OffsetRange.fromInclusiveEndpoints(startGroupCodepoint.get(),
          endGroupCodepointExclusive.get().shiftedCopy(-1)));
    } else {
      return Optional.absent();
    }
  }

  public CharOffset start() {
    return startMatchedCodepointForCodeUnit(wrappedMatcher.start());
  }

  public Optional<CharOffset> start(int group) {
    final int startCodeUnit = wrappedMatcher.start(group);
    if (startCodeUnit >= 0) {
      return Optional.of(startMatchedCodepointForCodeUnit(startCodeUnit));
    } else {
      return Optional.absent();
    }
  }


  public CharOffset endExclusive() {
    return endMatchedCodepointForCodeUnit(wrappedMatcher.end());
  }

  public Optional<CharOffset> endExclusive(int group) {
    final int endCodeUnit = wrappedMatcher.end(group);

    final boolean successfulMatchWithNoText = wrappedMatcher.start(group) == endCodeUnit;

    if (successfulMatchWithNoText) {
      // TODO: consider if this is the right thing to do here. Issue #70
      return Optional.absent();
    }

    if (endCodeUnit >= 0) {
      return Optional.of(endMatchedCodepointForCodeUnit(endCodeUnit));
    } else {
      return Optional.absent();
    }
  }

  public Optional<UnicodeFriendlyString> group(int groupIndex) {
    final Optional<OffsetRange<CharOffset>> offsets = matchOffsetsInclusive(groupIndex);
    if (offsets.isPresent()) {
      return Optional.of(matchedString.substringByCodePoints(offsets.get()));
    } else {
      return Optional.absent();
    }
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
