package com.bbn.bue.common;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Convert a string to a list of the Unicode codepoint numbers and names it
 * is made up of.
 *
 * @author Ryan Gabbard, Constantine Lignos
 */
public final class CodepointDumper {

  private CodepointDumper() {
  }

  public static CodepointDumper create() {
    return new CodepointDumper();
  }

  public ImmutableList<String> getCodepointNames(String s) {
    final ImmutableList.Builder<String> ret = ImmutableList.builder();
    for (int offset = 0; offset < s.length(); ) {
      final int codePoint = s.codePointAt(offset);
      String name = Character.getName(codePoint);
      if (name == null) {
        name = "U+" + Integer.toHexString(codePoint).toUpperCase() + " ("
            + Integer.toString(codePoint) + ")";
      }
      ret.add(name);
      offset += Character.charCount(codePoint);
    }
    return ret.build();
  }

  public static void main(String[] argv) throws IOException {
    System.out.println(StringUtils.unixNewlineJoiner().join(
        new CodepointDumper().getCodepointNames(
            Files.asCharSource(new File(argv[0]), Charsets.UTF_8).read())));
  }
}




