package com.bbn.bue.common.parameters;

import com.google.inject.AbstractModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Guice module to bind a {@link Parameters} instance.  It will be bound without annotation.
 *
 * If the optional parameter {@code com.bbn.logParameterAccesses} is set to true, the stack traces
 * of all parameter accesses will be logged upon program termination. See
 * {@link ParameterAccessListener} for details.
 */
public final class ParametersModule extends AbstractModule {

  private static final Logger log = LoggerFactory.getLogger(ParametersModule.class);

  private final Parameters parameters;

  private ParametersModule(final Parameters parameters) {
    this.parameters = checkNotNull(parameters);
  }

  /**
   * Creates a module which will bind {@link Parameters} to the provided {@code params} and will
   * also log the contents of the parameters to standard output at level {@code info}.
   */
  public static ParametersModule createAndDump(Parameters params) {
    log.info(params.dump());
    return new ParametersModule(params);
  }

  /**
   * Creates a module which will bind {@link Parameters} to the provided {@code params} without
   * doing any logging..
   */
  public static ParametersModule createSilently(Parameters params) {
    return new ParametersModule(params);
  }

  @Override
  protected void configure() {
    bind(Parameters.class).toInstance(parameters);
    // if requested, we can dump the stack traces of all parameter access at the end of execution
    if (parameters.getOptionalBoolean("com.bbn.logParameterAccesses").or(false)) {
      final ParameterAccessListener listener = ParameterAccessListener.create();
      parameters.registerListener(listener);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          listener.logParameterAccesses();
        }
      });
    }
  }
}
