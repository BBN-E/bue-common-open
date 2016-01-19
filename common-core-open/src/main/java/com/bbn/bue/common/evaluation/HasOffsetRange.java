package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.strings.offsets.Offset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.google.common.annotations.Beta;

/**
 * Anything that has an associated {@link OffsetRange}.
 */
@Beta
public interface HasOffsetRange<T extends Offset<T>> {
  OffsetRange<T> offsetRange();
}
