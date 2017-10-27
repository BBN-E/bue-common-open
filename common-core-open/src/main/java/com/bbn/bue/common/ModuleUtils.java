package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

/**
 * Private utilities for use by {@link ModuleFromParameter} and {@link ModuleIfPresent}.
 *
 * @author Ryan Gabbard
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ModuleUtils {

  private ModuleUtils() {
    throw new UnsupportedOperationException();
  }

  private static final ImmutableList<String> FALLBACK_INNER_CLASS_NAMES =
      ImmutableList.of("Module", "FromParametersModule", "FromParamsModule");

  public static Module classNameToModule(final Parameters parameters, final Class<?> clazz)
      throws IllegalAccessException, InvocationTargetException, InstantiationException {
    return classNameToModule(parameters, clazz, Optional.<Class<? extends Annotation>>absent());
  }

  /**
   * Attempts to convert a module class name to an instantiate module by applying heuristics to
   * construct it.
   *
   * It first tries to instantiate the provided class itself as a module, if possible. If it is not
   * a module, it looks for an inner class called "Module",
   * "FromParametersModule", or "FromParamsModule" which is a {@link Module}.
   *
   * When instantiating a module, it tries to find a constructor taking parameters and an annotation
   * (if annotation is present), just an annotation, just parameters, or zero arguments.
   */
  @Nonnull
  // it's reflection, can't avoid unchecked cast
  @SuppressWarnings("unchecked")
  public static Module classNameToModule(final Parameters parameters, final Class<?> clazz,
      Optional<? extends Class<? extends Annotation>> annotation)
      throws IllegalAccessException, InvocationTargetException, InstantiationException {
    if (Module.class.isAssignableFrom(clazz)) {
      return instantiateModule((Class<? extends Module>) clazz, parameters, annotation);
    } else {
      // to abbreviate the names of modules in param files, if a class name is provided which
      // is not a Module, we check if there is an inner-class named Module which is a Module
      for (final String fallbackInnerClassName : FALLBACK_INNER_CLASS_NAMES) {
        final String fullyQualifiedName = clazz.getName() + "$" + fallbackInnerClassName;
        final Class<? extends Module> innerModuleClazz;
        try {
          innerModuleClazz = (Class<? extends Module>) Class.forName(fullyQualifiedName);
        } catch (ClassNotFoundException cnfe) {
          // it's okay, we just try the next one
          continue;
        }

        if (Module.class.isAssignableFrom(innerModuleClazz)) {
          return instantiateModule(innerModuleClazz, parameters, annotation);
        } else {
          throw new RuntimeException(clazz.getName() + " is not a module; "
              + fullyQualifiedName + " exists but is not a module");
        }
      }

      // if we got here, we didn't find any module
      throw new RuntimeException("Could not find inner class of " + clazz.getName()
          + " matching any of " + FALLBACK_INNER_CLASS_NAMES);
    }
  }

  /**
   * Instantiates the given module class {@code clazz}.  First this looks for a constructor taking
   * {@link Parameters} as its only argument and uses it with the supplied {@code params} if
   * possible. Otherwise attempts to find and use a zero-arg constructor. Otherwise throws
   * a {@link RuntimeException}.
   */
  public static Module instantiateModule(final Class<? extends Module> clazz,
      final Parameters params)
      throws IllegalAccessException, InvocationTargetException, InstantiationException {
    return instantiateModule(clazz, params, Optional.<Class<? extends Annotation>>absent());
  }

  private static Module instantiateModule(final Class<? extends Module> clazz,
      final Parameters parameters,
      Optional<? extends Class<? extends Annotation>> annotation)
      throws InstantiationException, IllegalAccessException,
             InvocationTargetException {
    if (annotation.isPresent()) {
      // first we try a constructor which takes both
      final Optional<Module> withParamsAndAnnotationsConstructor =
          instantiateWithPrivateConstructor(clazz, new Class<?>[]{Parameters.class, Class.class},
              parameters, annotation.get());
      if (withParamsAndAnnotationsConstructor.isPresent()) {
        return withParamsAndAnnotationsConstructor.get();
      }

      // then annotations only
      final Optional<Module> withAnnotationsConstructor =
          instantiateWithPrivateConstructor(clazz, new Class<?>[]{Class.class}, annotation.get());
      if (withAnnotationsConstructor.isPresent()) {
        return withAnnotationsConstructor.get();
      }

      throw new RuntimeException("Tried to create module " + clazz + " when annotation " +
          annotation.get() + " was specified but it has neither"
          + " a (Parameters, Class<? extends Annotation>) constructor nor a "
          + "(Class<? extends Annotation>) constructor.");
    } else {
      final Optional<Module> withParamsConstructor =
          instantiateWithPrivateConstructor(clazz, new Class<?>[]{Parameters.class}, parameters);
      if (withParamsConstructor.isPresent()) {
        return withParamsConstructor.get();
      } else {
        final Optional<Module> withNoArgConstructor =
            instantiateWithPrivateConstructor(clazz, new Class<?>[]{});
        if (withNoArgConstructor.isPresent()) {
          return withNoArgConstructor.get();
        } else {
          throw new RuntimeException("Tried to create module " + clazz + " but it has neither"
              + " a zero-argument constructor or one taking only a parameters object");
        }
      }
    }
  }

  /**
   * Instantiates a module which may have a private constructor.  You can't call private
   * constructors in Java unless you first make the constructor un-private.  After doing so,
   * however, we should clean up after ourselves and restore the previous state.
   *
   * If we fail to instantiate the class, we return absent.
   */
  private static Optional<Module> instantiateWithPrivateConstructor(Class<?> clazz,
      Class<?>[] parameters,
      Object... paramVals)
      throws InvocationTargetException, IllegalAccessException, InstantiationException {
    final Constructor<?> constructor;
    try {
      constructor = clazz.getDeclaredConstructor(parameters);
    } catch (NoSuchMethodException e) {
      return Optional.absent();
    }

    final boolean oldAccessible = constructor.isAccessible();
    constructor.setAccessible(true);
    try {
      return Optional.of((Module) constructor.newInstance(paramVals));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw e;
    } finally {
      constructor.setAccessible(oldAccessible);
    }
  }

  /**
   * A module which installs others modules.
   */
  static class MultiModule extends AbstractModule {

    private final ImmutableList<Module> modules;

    public MultiModule(final Iterable<? extends Module> modules) {
      this.modules = ImmutableList.copyOf(modules);
    }

    @Override
    protected void configure() {
      for (final Module module : modules) {
        install(module);
      }
    }
  }
}
