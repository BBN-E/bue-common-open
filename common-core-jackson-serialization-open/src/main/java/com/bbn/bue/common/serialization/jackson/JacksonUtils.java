package com.bbn.bue.common.serialization.jackson;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.List;

public final class JacksonUtils {
    private JacksonUtils() {
        throw new UnsupportedOperationException();
    }

    private static final Supplier<List<Module>> discoveredModules = Suppliers.memoize(
       new Supplier<List<Module>>() {
          @Override
          public List<Module> get() {
              return ObjectMapper.findModules();
          }
       }
    );

    /**
     * Caches the results of {@link com.fasterxml.jackson.databind.ObjectMapper#findModules()}.
     * Note that if the classes available to your application change after the first call to
     * these, the returned list will be incorrect.
     * @return
     */
    public static synchronized Supplier<List<Module>> discoverModulesWithCaching() {
        return discoveredModules;
    }
}
