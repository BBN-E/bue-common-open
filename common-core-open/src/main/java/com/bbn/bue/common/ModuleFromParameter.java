package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.Module;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Creates a Guice module from a parameter.  The parameter is expected to contain the class name
 * of the module or modules to create.  You may specify a default class or classes to be used
 * if the parameter is absent. The modules must be constructable either by a zero-argument
 * constructor, one which takes a single {@link Parameters} object, one which takes a single
 * annotation object, or one which takes both a parameters and an annotation.
 *
 * If created with {@link #forParameter(String)}, a single class name will be expected as
 * the parameter value and a single module will be created.
 *
 * If created with {@link #forMultiParameter(String)}, multiple class names will be allowed
 * and the returned module will be an anonymous module which simply installs all the created
 * modules. This is useful in conjunction with Guice Multibindings.
 *
 * Being able to pass an annotation in to the constructor can be useful when you may want
 * to use the same module to bind multiple sites (e.g. binding multiple string normalizers
 * by using the same modules with different annotations).
 *
 * @author Ryan Gabbard
 */
public final class ModuleFromParameter {

  final String parameter;
  final ImmutableList<Class<?>> defaultClasses;
  @Nullable
  final Class<? extends Annotation> annotationClass;
  @Nullable
  final Module defaultModule;
  final boolean allowMultiple;

  private ModuleFromParameter(final String parameter,
      final Iterable<? extends Class<?>> defaultClasses,
      @Nullable final Module defaultModule,
      @Nullable final Class<? extends Annotation> annotationClass,
      boolean allowMultiple) {
    this.parameter = checkNotNull(parameter);
    checkArgument(!parameter.isEmpty(), "Cannot extract module(s) from empty parameter name");
    this.annotationClass = annotationClass;
    this.defaultClasses = ImmutableList.copyOf(defaultClasses);
    this.defaultModule = defaultModule;
    checkArgument(defaultModule == null || this.defaultClasses.isEmpty(),
        "May not specify both default module classes and a default module instance");
    this.allowMultiple = allowMultiple;
    checkArgument(allowMultiple || this.defaultClasses.size() < 2, "Cannot have multiple defaults "
        + "if multiple bindings are not allowed");
  }

  public static ModuleFromParameter forParameter(String parameter) {
    return new ModuleFromParameter(parameter, ImmutableList.<Class<?>>of(), null, null, false);
  }

  public static ModuleFromParameter forMultiParameter(String parameter) {
    return new ModuleFromParameter(parameter, ImmutableList.<Class<?>>of(), null, null, true);
  }

  public ModuleFromParameter withNoOpDefault() {
    return withDefault(NoOpModule.class);
  }

  public ModuleFromParameter withDefault(Class<?> defaultClass) {
    return new ModuleFromParameter(parameter, ImmutableList.<Class<?>>of(defaultClass),
        defaultModule, annotationClass, allowMultiple);
  }

  public ModuleFromParameter withDefault(Module defaultModule) {
    return new ModuleFromParameter(parameter, defaultClasses, defaultModule, annotationClass,
        allowMultiple);
  }

  public ModuleFromParameter withDefaults(Iterable<? extends Class<?>> defaultClasses) {
    checkState(allowMultiple, "Cannot have multiple defaults if multiple bindings "
        + "are not permitted");
    return new ModuleFromParameter(parameter, defaultClasses, defaultModule, annotationClass,
        allowMultiple);
  }

  public ModuleFromParameter withAnnotation(Class<? extends Annotation> annotation) {
    return new ModuleFromParameter(parameter, defaultClasses, defaultModule, annotation,
        allowMultiple);
  }

  public Module extractFrom(Parameters parameters) {
    final ImmutableList<Class<?>> classes;
    if (parameters.isPresent(parameter)) {
      if (allowMultiple) {
        classes = parameters.getClassObjects(parameter);
      } else {
        classes = ImmutableList.<Class<?>>of(parameters.getClassObject(parameter));
      }
    } else if (!defaultClasses.isEmpty()) {
      classes = defaultClasses;
    } else if (defaultModule != null) {
      return defaultModule;
    } else {
      throw new RuntimeException("Missing required parameter " + parameter);
    }

    final ImmutableList.Builder<Module> modulesB = ImmutableList.builder();
    for (final Class<?> clazz : classes) {
      try {
        modulesB.add(ModuleUtils.classNameToModule(parameters, clazz,
            Optional.fromNullable(annotationClass)));
      } catch (IllegalAccessException | InstantiationException | InvocationTargetException e1) {
        throw new RuntimeException("Tried to create module " + clazz + " but it has neither"
            + " a zero-arugment constructor or one taking only a parameters object");
      }
    }

    final ImmutableList<Module> modules = modulesB.build();
    if (modules.size() == 1) {
      return modules.get(0);
    } else {
      return new ModuleUtils.MultiModule(modules);
    }
  }

}


