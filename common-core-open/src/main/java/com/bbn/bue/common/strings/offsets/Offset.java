package com.bbn.bue.common.strings.offsets;

public interface Offset {
    /**
     * Prefer {@link #asInt()}
     * @return
     */
    @Deprecated
    public int value();
    public int asInt();
}
