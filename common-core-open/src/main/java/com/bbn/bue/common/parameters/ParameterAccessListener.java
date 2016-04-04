package com.bbn.bue.common.parameters;

import com.bbn.bue.common.StringUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Logs the stack traces of all parameter access and can dump them when requested.
 */
final class ParameterAccessListener implements Parameters.Listener {

  private static final Logger log = LoggerFactory.getLogger(ParameterAccessListener.class);

  private final SetMultimap<String, String> paramToStackTrace = HashMultimap.create();

  private ParameterAccessListener() {

  }

  public static ParameterAccessListener create() {
    return new ParameterAccessListener();
  }

  @Override
  public void observeParameterRequest(final String param) {
    // for each parameter access, record the accessing stack trace
    final String interestingStackTraceString =
        FluentIterable.from(Arrays.asList(Thread.currentThread().getStackTrace()))
            // but exclude code in Parameters itself
            .filter(new Predicate<StackTraceElement>() {
              @Override
              public boolean apply(@Nullable final StackTraceElement input) {
                return !input.getClassName().contains("com.bbn.bue.common.parameters");
              }
            }).join(StringUtils.unixNewlineJoiner());
    paramToStackTrace.put(param, interestingStackTraceString);
  }

  public void logParameterAccesses() {
    final StringBuilder msg = new StringBuilder();
    for (final Map.Entry<String, String> e : paramToStackTrace.entries()) {
      msg.append("Parameter ").append(e.getKey()).append(" access at \n")
          .append(e.getValue()).append("\n\n");
    }
    log.info(msg.toString());
  }
}
