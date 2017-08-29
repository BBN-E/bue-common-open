package com.bbn.nlp.io;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This should be merged into the newer {@link com.bbn.bue.common.files.KeyValueSource}
 * code.
 *
 * @author Ryan Gabbard
 */
public final class CachingOriginalTextSource implements OriginalTextSource {

  private final LoadingCache<Symbol, String> cache;

  private CachingOriginalTextSource(
      final LoadingCache<Symbol, String> cache) {
    this.cache = checkNotNull(cache);
  }

  public static OriginalTextSource from(final OriginalTextSource baseSource, int maxToCache) {
    return new CachingOriginalTextSource(
        CacheBuilder.newBuilder()
            .maximumSize(maxToCache)
            .<Symbol, String>build(new CacheLoader<Symbol, String>() {
              @Override
              public String load(final Symbol key) throws Exception {
                final Optional<String> baseRet = baseSource.getOriginalText(key);
                if (baseRet.isPresent()) {
                  return baseRet.get();
                } else {
                  return null;
                }
              }
            }));
  }

  @Override
  public Optional<String> getOriginalText(final Symbol docID) throws IOException {
    try {
      return Optional.fromNullable(cache.get(docID));
    } catch (final ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw new RuntimeException(e);
      }
    }
  }
}
