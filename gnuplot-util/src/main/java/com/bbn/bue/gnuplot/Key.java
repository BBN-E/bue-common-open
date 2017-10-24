package com.bbn.bue.gnuplot;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Key {

  private final boolean visible;
  private final Insideness insideness;
  private final Orientation orientation;
  private final boolean box;

  public Key(boolean visible, Insideness insideness, Orientation orientation, boolean box) {
    this.visible = visible;
    this.insideness = checkNotNull(insideness);
    this.orientation = checkNotNull(orientation);
    this.box = box;
  }

  public void appendPlotCommands(PlotBundle.Builder pb) {
    if (!visible) {
      pb.append("set key off\n");
    } else {
      pb.append("set key on ");
      pb.append(insideness.command());
      pb.append(" ");
      pb.append(orientation.command());
      if (box) {
        pb.append(" box");
      } else {
        pb.append(" nobox");
      }
      pb.append("\n");
    }
  }

  public static Key hiddenKey() {
    return new Key(false, Insideness.Inside, Orientation.Vertical, false);
  }

  public static Builder visibleKey() {
    return new Builder(true);
  }

  public static final class Builder {

    private final boolean visible;
    private Insideness insideness = Insideness.Inside;
    private Orientation orientation = Orientation.Vertical;
    private boolean box = true;

    public Builder(boolean visible) {
      this.visible = visible;
    }

    public Builder inside() {
      this.insideness = Insideness.Inside;
      return this;
    }

    public Builder outside() {
      this.insideness = Insideness.Outside;
      return this;
    }

    public Builder horizontal() {
      this.orientation = Orientation.Horizontal;
      return this;
    }

    public Builder vertical() {
      this.orientation = Orientation.Vertical;
      return this;
    }

    public Builder withBox() {
      this.box = true;
      return this;
    }

    public Builder withoutBox() {
      this.box = false;
      return this;
    }

    public Key build() {
      return new Key(visible, insideness, orientation, box);
    }
  }

  private enum Insideness {
    Inside {
      @Override
      public String command() {
        return "inside";
      }
    }, Outside {
      @Override
      public String command() {
        return "outside";
      }
    };

    public abstract String command();
  }

  private enum Orientation {
    Vertical {
      @Override
      public String command() {
        return "vertical";
      }
    }, Horizontal {
      @Override
      public String command() {
        return "horizontal";
      }
    };

    public abstract String command();
  }

}
