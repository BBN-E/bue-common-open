/**
 * Various utilities for working with file I/O.
 *
 * The major bits of code here are:
 *
 * <ul>
 *
 * <li><b>{@link com.bbn.bue.common.files.FileUtils}:</b> various utility methods for working with
 * files.</li>
 *
 * <li<b>{@link com.bbn.bue.common.files.KeyValueSource}, {@link com.bbn.bue.common.files.KeyValueSink}
 * and related classes:</b> an implementation-agnostic way of working with mappings between keys and
 * values. In practice we largely use these for mapping between document IDs and documents without
 * worrying about whether the storage is a filesystem, database, or something else. These should be
 * instantiated via {@link com.bbn.bue.common.files.KeyValueSources} and {@link
 * com.bbn.bue.common.files.KeyValueSinks}</li>
 *
 * </ul>
 *
 * The are some additional minor utilities:
 *
 * <ul> <li><b>Tool to split a document corpus:</b> {@link com.bbn.bue.common.files.SplitCorpus}</li>
 * <li><b>Class to load three column tab-separated files as a {@link
 * com.bbn.bue.common.collections.Multitable}</b>: {@link com.bbn.bue.common.files.MultitableLoader}</li>
 * <li><b>Working with document lists:</b> {@link com.bbn.bue.common.files.MergeFileLists}, {@link
 * com.bbn.bue.common.files.SubtractFileLists}</li> <li><b>Working with document Id to file
 * maps:</b> {@link com.bbn.bue.common.files.DocIDToFileMapContains}, {@link
 * com.bbn.bue.common.files.SubtractFileMaps}</li>
 *
 * </ul>
 */
@ParametersAreNonnullByDefault
package com.bbn.bue.common.files;

import javax.annotation.ParametersAreNonnullByDefault;
