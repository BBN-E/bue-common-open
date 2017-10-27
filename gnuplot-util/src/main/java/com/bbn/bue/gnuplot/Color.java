package com.bbn.bue.gnuplot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Color {

  private final String colorString;

  private Color(String colorString) {
    this.colorString = checkNotNull(colorString);
    checkArgument(colorString.length() == 7 && colorString.charAt(0) == '#');
    for (int i = 1; i < 7; ++i) {
      final char c = colorString.charAt(i);
      checkArgument(Character.isDigit(c) || (c >= 'a' && c <= 'f')
          || (c >= 'A' && c <= 'F'));
    }
  }

  public static Color fromHexString(String colorString) {
    return new Color(colorString);
  }

  public String asColorString() {
    return colorString;
  }

  public String asQuotedColorString() {
    return "\"" + asColorString() + "\"";
  }
}
