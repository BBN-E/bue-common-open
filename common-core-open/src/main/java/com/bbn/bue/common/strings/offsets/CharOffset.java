package com.bbn.bue.common.strings.offsets;

import com.google.common.primitives.Ints;

public final class CharOffset extends AbstractOffset implements Comparable<CharOffset> {
	public CharOffset(final int val) {
		super(val);
	}

	public static CharOffset asCharOffset(final int val) {
		return new CharOffset(val);
	}


    @Override
    public boolean equals(final Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

	@Override
	public int compareTo(final CharOffset o) {
		return Ints.compare(value(), o.value());
	}

	@Override
	public String toString() {
		return "c" + Integer.toString(value());
	}
}