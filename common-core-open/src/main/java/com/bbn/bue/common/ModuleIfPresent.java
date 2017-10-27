package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Module;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Installs specifies module(s) if a specified parameter is present.
 *
 * @author Ryan Gabbard
 */
public final class ModuleIfPresent {

  private final List<Class<?>> classes;

  private ModuleIfPresent(final String parameter,
      final List<Class<?>> classes) {
    this.classes = Lists.newArrayList(classes);
  }

  public static ModuleIfPresent module(Class<?> clazz) {
    return new ModuleIfPresent(null, ImmutableList.<Class<?>>of(clazz));
  }

  public static ModuleIfPresent modules(Class<?> clazz1,
      Class<?>... clazzes) {
    final List<Class<?>> ret = new ArrayList<>();
    ret.add(clazz1);
    for (final Class<?> clazz : clazzes) {
      ret.add(clazz);
    }

    return new ModuleIfPresent(null, ret);
  }

  public Stage2 ifPresent(String param) {
    return new Stage2(param);
  }

  public class Stage2 {

    private String parameter;

    Stage2(final String parameter) {
      this.parameter = checkNotNull(parameter);
    }

    public Module in(Parameters params) {
      if (params.isPresent(parameter)) {
        final ImmutableList.Builder<Module> ret = ImmutableList.builder();
        for (final Class<?> clazz : classes) {
          try {
            ret.add(ModuleUtils.classNameToModule(params, clazz));
          } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException("Error instantiating " + clazz.getName() + " for parameter "
                + parameter);
          }
        }
        return new ModuleUtils.MultiModule(ret.build());
      } else {
        return new NoOpModule();
      }
    }
  }
}
