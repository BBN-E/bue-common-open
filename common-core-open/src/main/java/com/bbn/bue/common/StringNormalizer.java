package com.bbn.bue.common;

/**
 * A string normalizer is any strategy for mapping a sequence of characters to another
 * sequence of characters, where typically the output sequence represents some
 * equivalence class over the inputs.
 *
 * This has several uses:
 * <ul>
 * <li>Unicode normalization</li>
 * <li>Word shape features:  A typical example would be a rule like:
 * "Map all alphabetical characters to A, map all digits to D,
 * keep all other characters the same, and collapse adjacent repeated characters."
 * This would map 617-873-8000 to D-D-D and attorney-general to A-A.</li>
 * </ul>
 */
public interface StringNormalizer {

  /**
   * Map a string to its word shape. Neither the input nor the output may
   * be {@code null}.
   */
  String normalize(String input);
}
