package com.bbn.bue.common;

/**
 * Indicates that the implemented object provides a hashCode (obtainable from {@link #stableHashCode()}
 * which is consistent across program executions. This does not mean its {@code hashCode} method is
 * stable.  There is no guarantee the stable hash code has any particular relationship to the
 * {@link java.lang.Object#equals(Object)} method.
 *
 * This is mainly useful for feature hashing.
 */
public interface HasStableHashCode {

  /**
   * A stable hash code which is consistent across program executions.
   */
  public int stableHashCode();
}
