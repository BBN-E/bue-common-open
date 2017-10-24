package com.bbn.bue.gnuplot;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.util.List;

public final class Palette {
  private ImmutableList<Color> colorList;

  private Palette(final List<Color> colors) {
    this.colorList = ImmutableList.copyOf(colors);
  }

  public static Palette from(final List<Color> colors) {
    return new Palette(colors);
  }

  public Iterable<Color> infinitePaletteLoop() {
    return Iterables.cycle(colorList);
  }
}
