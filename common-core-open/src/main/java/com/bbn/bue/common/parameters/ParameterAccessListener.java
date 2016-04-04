package com.bbn.bue.common.parameters;

import com.bbn.bue.common.StringUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSetMultimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.compose;
import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;

/**
 * Logs the stack traces of all parameter access and can dump them when requested.
 */
final class ParameterAccessListener implements Parameters.Listener {

  private static final Logger log = LoggerFactory.getLogger(ParameterAccessListener.class);

  private final ImmutableSetMultimap.Builder<String, List<StackTraceElement>> paramToStackTrace =
      ImmutableSetMultimap.builder();

  private ParameterAccessListener() {

  }

  public static ParameterAccessListener create() {
    return new ParameterAccessListener();
  }

  @Override
  public void observeParameterRequest(final String param) {
    // for each parameter access, record the accessing stack trace
    paramToStackTrace.put(param, Arrays.asList(Thread.currentThread().getStackTrace()));
  }

  public void logParameterAccesses() {
    log.info(constructLogMsg());
  }

  // pulled into method for testing
  String constructLogMsg() {
    final StringBuilder msg = new StringBuilder();

    for (final Map.Entry<String, List<StackTraceElement>> e : paramToStackTrace.build().entries()) {
      msg.append("Parameter ").append(e.getKey()).append(" accessed at \n");
      msg.append(FluentIterable.from(e.getValue())
          // but exclude code in Parameters itself
          .filter(not(IS_THIS_CLASS))
          .filter(not(IS_PARAMETERS_ITSELF))
          .filter(not(IS_THREAD_CLASS))
          .join(StringUtils.unixNewlineJoiner()));
      msg.append("\n\n");
    }
    return msg.toString();
  }


  // move this to a StackTraceUtils at some point
  private static final Function<StackTraceElement, String> CLASS_NAME =
      new Function<StackTraceElement, String>() {
        @Override
        public String apply(final StackTraceElement input) {
          return input.getClassName();
        }
      };

  private static final Predicate<StackTraceElement> IS_THIS_CLASS =
      compose(equalTo("com.bbn.bue.common.parameters.ParameterAccessListener"), CLASS_NAME);
  private static final Predicate<StackTraceElement> IS_PARAMETERS_ITSELF =
      compose(equalTo("com.bbn.bue.common.parameters.Parameters"), CLASS_NAME);
  private static final Predicate<StackTraceElement> IS_THREAD_CLASS =
      compose(equalTo("java.lang.Thread"), CLASS_NAME);
}
