package com.bbn.bue.common.strings.offsets;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OffsetGroupSpan {

  private final OffsetGroupRange range;
  private final Symbol type;
  private final ImmutableMap<String, String> attributes;

  public OffsetGroupRange range() {
    return range;
  }

  public Symbol type() {
    return type;
  }

  public Map<String, String> attributes() {
    return attributes;
  }


	/*private static final Symbol REGION_SPAN = Symbol.from("RegionSpan");

	public static OffsetGroupSpan createRegionSpan(final OffsetGroupRange range) {
		return new OffsetGroupSpan(REGION_SPAN, range, ImmutableMap.<String,String>of());
	}*/

  public static OffsetGroupSpan create(final Symbol spanType, final OffsetGroupRange range,
      final Map<String, String> otherAttributes) {
    return new OffsetGroupSpan(spanType, range, otherAttributes);
  }

  public static OffsetGroupSpan create(final Symbol spanType, final OffsetGroupRange range) {
    return create(spanType, range, ImmutableMap.<String, String>of());
  }

  private OffsetGroupSpan(final Symbol spanType, final OffsetGroupRange range,
      final Map<String, String> otherAttributes) {
    this.range = checkNotNull(range);
    this.type = checkNotNull(spanType);
    this.attributes = ImmutableMap.copyOf(otherAttributes);
  }

  public static Function<OffsetGroupSpan, OffsetGroupRange> toOffsetGroupRangeFunction() {
    return new Function<OffsetGroupSpan, OffsetGroupRange>() {
      @Override
      public OffsetGroupRange apply(OffsetGroupSpan input) {
        return input.range();
      }
    };
  }
}
