package com.bbn.bue.common;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.parameters.ParametersModule;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.grapher.graphviz.GraphvizGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Nullable;

/**
 * Methods for running {@link TextGroupEntryPoint}. See {@link #runEntryPoint(Class, Class,
 * String[])} for primary documentation.
 *
 * @author Ryan Gabbard
 */
public final class TextGroupEntryPoints {

  private static final Logger log = LoggerFactory.getLogger(TextGroupEntryPoints.class);

  private TextGroupEntryPoints() {
    throw new UnsupportedOperationException();
  }

  /**
   * Runs a {@link TextGroupEntryPoint}.  The entry point class will be injected using a {@link
   * Injector} configured by two modules: a {@link com.bbn.bue.common.parameters.ParametersModule}
   * initialized by loading the first (and required-to-be-only) argument in {@code args} and an
   * instance of {@code configModuleClass} instantiated according to the rules of {@link
   * ModuleUtils#instantiateModule(Class, Parameters)}.  This method will then call {@link
   * TextGroupEntryPoint#run()} on the injected entry point object.
   *
   * This method also provides some convenient features for debugging, controlled by parameters:
   *
   * <ul>
   *
   * <li>{@code ccom.bbn.bue.common.debug.allowExceptionsToPassUncaught}: (default false) In
   * production use, we always want to catch exceptions and explicitly exit to ensure a non-zero
   * exit code (which the JVM does not guarantee).  However, sometimes in debugging it is useful to
   * suppress this behavior, because it blocks IntelliJ's useful "break on uncaught exceptions only"
   * option. Therefore we allow the user to disable it with this parameter.</li>
   *
   * <li>{@code com.bbn.bue.common.debug.graphGuiceDependenciesTo}: (default absent) If specified, a
   * {@code dot} file for the Guice dependencies will be written to the specified file. If {@code
   * com.bbn.bue.common.debug.skipExecution} is specified and true, the normal execution of the
   * program will be skipped.</li>
   *
   * </ul>
   */
  public static void runEntryPoint(Class<? extends TextGroupEntryPoint> entryPointClass,
      Class<? extends Module> configModuleClass, String[] args) throws Exception {
    runEntryPointInternal(entryPointClass, args, configModuleClass);
  }

  /**
   * Like {@link #runEntryPoint(Class, Class, String[])} but auto-detects the configuration module
   * by searching {@code entryPointClass} for an inner class named {@code Module} or
   * {@code FromParametersModule} (or any name supported by
   * {@link ModuleUtils#classNameToModule(Parameters, Class)}
   */
  public static void runEntryPoint(Class<? extends TextGroupEntryPoint> entryPointClass,
      String[] args) throws Exception {
    runEntryPointInternal(entryPointClass, args, null);
  }

  private static final String EXCEPTION_PASSTHROUGH_PARAM =
      "com.bbn.bue.common.debug.allowExceptionsToPassUncaught";

  private static void runEntryPointInternal(Class<? extends TextGroupEntryPoint> entryPointClass,
      String[] args, @Nullable Class<? extends Module> configModuleClass) throws Exception {
    if (args.length != 1) {
      System.err.println(entryPointClass.getName() + " takes a single argument, a parameter file.");
      System.exit(1);
    }

    // load the parameter file.
    final Parameters params;
    try {
      params = Parameters.loadSerifStyle(new File(args[0]));
    } catch (Exception e) {
      // ensure that if an exception is thrown, we return a non-zero exit code
      // this is not otherwise guaranteed by the JVM
      e.printStackTrace();
      System.exit(1);
      // unreachable return is necessary to prevent compilation error on params being potentially
      // uninitialized
      return;
    }

    // in production use, we always want to catch exceptions and explicitly exit
    // to ensure a non-zero exit code (which the JVM does not guarantee).  However,
    // sometimes in debugging it is useful to suppress this behavior, because it blocks
    // IntelliJ's useful "break on uncaught exceptions only" option.  Therefore we allow
    // the user to disable it with the parameter below. Handling this is also why we have
    // two distinct try/catch blocks in this method and shift the remaining work to another
    // method.
    final boolean allowExceptionsToPassThrough =
        params.getOptionalBoolean(EXCEPTION_PASSTHROUGH_PARAM).or(false);
    if (allowExceptionsToPassThrough) {
      log.warn("Top-level catching of exceptions suppressed in debugging.  If you see this "
          + "in production, turn off the {} parameter", EXCEPTION_PASSTHROUGH_PARAM);
      internalExecute(entryPointClass, configModuleClass, params);
    } else {
      try {
        internalExecute(entryPointClass, configModuleClass, params);
      } catch (Exception e) {
        e.printStackTrace();
        System.exit(1);
      }
    }
  }

  private static final String SKIP_EXECUTION_PARAM = "com.bbn.bue.common.debug.skipExecution";

  private static void internalExecute(final Class<? extends TextGroupEntryPoint> entryPointClass,
      @Nullable final Class<? extends Module> configModuleClass, final Parameters params)
      throws Exception {
    // the configuration module class can be specified or auto-detected, depending on which
    // public method the user chose
    final Module configModule;
    if (configModuleClass != null) {
      configModule = ModuleUtils.instantiateModule(configModuleClass, params);
    } else {
      configModule = ModuleUtils.classNameToModule(params, entryPointClass);
    }

    final Injector injector = Guice.createInjector(ParametersModule.createAndDump(params),
        configModule);

    // if requested, produce a GraphViz graph of Guice dependencies before execution
    maybeGraphGuiceDependencies(injector, entryPointClass, params);

    // actually run the program, unless requested to skip this
    if (!params.getOptionalBoolean(SKIP_EXECUTION_PARAM).or(false)) {
      injector.getInstance(entryPointClass).run();
    } else {
      // why would you want to skip execution? Currently, the only reason is to graph Guice
      // dependencies without executing, so for safety we check for that parameter.
      if (!params.isPresent(GRAPH_DEPENDENCIES_PARAM)) {
        log.warn("Skipping execution due to {}, but {} not specified. Why?",
            SKIP_EXECUTION_PARAM, GRAPH_DEPENDENCIES_PARAM);
        // we exit explicitly because there is no need for the clutter of a stack trace
        System.exit(1);
      }
      // otherwise do nothing and exit
    }
  }

  private static final String GRAPH_DEPENDENCIES_PARAM =
      "com.bbn.bue.common.debug.graphGuiceDependenciesTo";

  private static void maybeGraphGuiceDependencies(final Injector injector,
      final Class<? extends TextGroupEntryPoint> entryPointClass, final Parameters params)
      throws IOException {

    final Optional<File> dotFile = params.getOptionalCreatableFile(GRAPH_DEPENDENCIES_PARAM);

    if (dotFile.isPresent()) {
      final Injector grapherInjector = Guice.createInjector(new GraphvizModule());
      final GraphvizGrapher grapher = grapherInjector.getInstance(GraphvizGrapher.class);
      // orient graph vertically
      grapher.setRankdir("TB");

      log.info("Writing Guice configuration graph to {}. To compile it, do dot -T png {}",
          dotFile.get(), dotFile.get());
      try (PrintWriter out = new PrintWriter(Files.asCharSink(dotFile.get(), Charsets.UTF_8)
          .openBufferedStream())) {
        grapher.setOut(out);
        grapher.graph(injector, ImmutableSet.<Key<?>>of(Key.get(entryPointClass)));
      }
    }
  }
}
