/**
 * Utilities related to collections.
 *
 * Major hunks of code:
 *
 * <ul>
 *
 *
 * <li><b>{@link com.bbn.bue.common.collections.Multitable} and friends:</b> Combines a Guava {@link
 * com.bbn.bue.common.collections.Multitable} and a {@link com.google.common.collect.Multimap} to
 * allow mapping a pair of keys to multiple values.</li>
 *
 * <li><b>Tools to make building {@link com.google.common.collect.ImmutableMap}s easier:</b> {@link
 * com.google.common.collect.ImmutableMap} is great not only because it is immutable but also
 * because it has a deterministic iteration order. But its builder is often overly strict for
 * particular applications - for example, forbidding duplicate key-value pairs being added even if
 * identical.  We provide a number of more relaxed builders including {@link
 * com.bbn.bue.common.collections.MapUtils#immutableMapBuilderIgnoringDuplicates()}, {@link
 * com.bbn.bue.common.collections.MapUtils#immutableMapBuilderResolvingDuplicatesBy(java.util.Comparator)},
 * and {@link com.bbn.bue.common.collections.MapUtils#immutableMapBuilderAllowingSameEntryTwice()}.
 * See {@link com.bbn.bue.common.collections.LaxImmutableMapBuilder} for more details. </li>
 *
 * <li>Utilities for most major collection types: {@link com.bbn.bue.common.collections.CollectionUtils},
 * {@link com.bbn.bue.common.collections.IterableUtils}, {@link com.bbn.bue.common.collections.IteratorUtils},
 * {@link com.bbn.bue.common.collections.ListUtils}, {@link com.bbn.bue.common.collections.MapUtils},
 * {@link com.bbn.bue.common.collections.MultimapUtils}, {@link com.bbn.bue.common.collections.MultisetUtils},
 * {@link com.bbn.bue.common.collections.RangeUtils}, {@link com.bbn.bue.common.collections.SetUtils},
 * and {@link com.bbn.bue.common.collections.TableUtils}.</li>
 *
 * </ul>
 *
 * Minor hunks of code: generating samples for bootstrap confidence estimation ({@link
 * com.bbn.bue.common.collections.BootstrapIterator}, shuffling things ({@link
 * com.bbn.bue.common.collections.ShufflingIterable}, {@link com.bbn.bue.common.collections.ShufflingCollection}),
 * and sets of potentially overlapping ranges ({@link com.bbn.bue.common.collections.OverlappingRangeSet}).
 */
package com.bbn.bue.common.collections;
