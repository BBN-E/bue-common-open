package com.bbn.bue.common.parameters;

import com.bbn.bue.common.parameters.serifstyle.SerifStyleParameterFileLoader;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Given two parameter files, logs those params only in the left (prefixed by "<"),
 * those only in the right (prefixed by "<"), and those in both with conflicting values
 * (prefixed by "<>").
 *
 * Usage: ParametersDiff [leftParamFile] [rightParamFile]
 */
public final class ParametersDiff {

  private ParametersDiff() {
    throw new UnsupportedOperationException();
  }

  public static void main(String[] argv) {
    // we wrap the main method in this way to
    // ensure a non-zero return value on failure
    try {
      trueMain(argv);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(String[] argv) throws IOException {
    final SerifStyleParameterFileLoader paramFileLoader =
        new SerifStyleParameterFileLoader.Builder()
            .crashOnUndeclaredOverrides(false).build();
    final Parameters leftParams = paramFileLoader.load(new File(argv[0]));
    final Parameters rightParams = paramFileLoader.load(new File(argv[1]));

    dumpOnlyIn("< ", Sets.difference(leftParams.asMap().keySet(), rightParams.asMap().keySet()),
        leftParams);
    dumpOnlyIn("> ", Sets.difference(rightParams.asMap().keySet(), leftParams.asMap().keySet()),
        rightParams);
    dumpConflicts(leftParams, rightParams);
  }

  private static void dumpOnlyIn(final String prefix, final Set<String> uniqueKeys,
      final Parameters params) {
    for (final String uniqueKey : Ordering.natural().sortedCopy(uniqueKeys)) {
      System.out.println(prefix + uniqueKey + ": " + params.getString(uniqueKey));
    }
  }

  private static void dumpConflicts(final Parameters leftParams, final Parameters rightParams) {
    final List<String> potentialConflicts = Ordering.natural()
        .sortedCopy(Sets.intersection(leftParams.asMap().keySet(), rightParams.asMap().keySet()));
    for (final String potentialConflict : potentialConflicts) {
      final String leftVal = leftParams.getString(potentialConflict);
      final String rightVal = rightParams.getString(potentialConflict);

      if (!leftVal.equals(rightVal)) {
        System.out.println("<> " + potentialConflict +
            "\nleft: " + leftVal +
            "\nright: " + rightVal);
      }
    }
  }
}
