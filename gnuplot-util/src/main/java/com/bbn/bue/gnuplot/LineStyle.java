package com.bbn.bue.gnuplot;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class LineStyle {

  private final Color color;
  private final LineType lineType;
  private final double width;
  private final PointType pointType;
  private final double pointSize;

  private LineStyle(LineType lineType, PointType pointType, Color color,
      double width, double pointSize) {
    this.lineType = checkNotNull(lineType);
    this.pointType = checkNotNull(pointType);
    this.color = checkNotNull(color);
    this.width = width;
    checkArgument(width > 0.0);
    this.pointSize = pointSize;
    checkArgument(pointSize >= 0.0);
  }

  public static Builder builder() {
    return new Builder();
  }

  public void appendPlotCommands(PlotBundle.Builder sb) {
    sb.append("lt ").append(lineType.asGnuPlotCommand())
        .append(" lc ").append(color.asQuotedColorString())
        .append(" lw ").append(width)
        .append(" pt ").append(pointType.asGnuPlotCommand())
        .append(" ps ").append(pointSize);
  }

  public static class Builder {

    private Color color = Color.fromHexString("#000000");
    private LineType lineType = LineType.number(0);
    private double width = 1;
    private PointType pointType = PointType.number(0);
    private double pointSize = 1;

    public Builder setColor(Color color) {
      this.color = checkNotNull(color);
      return this;
    }

    public Builder setLineType(LineType lineType) {
      this.lineType = checkNotNull(lineType);
      return this;
    }

    public Builder setWidth(double width) {
      this.width = width;
      return this;
    }

    public Builder setPointType(PointType pointType) {
      this.pointType = pointType;
      return this;
    }

    public Builder pointSize(double pointSize) {
      this.pointSize = pointSize;
      return this;
    }

    public LineStyle build() {
      return new LineStyle(lineType, pointType, color, width, pointSize);
    }
  }

    /*
     set style line <index> default
     set style line <index> {{linetype  | lt} <line_type> | <colorspec>}
                            {{linecolor | lc} <colorspec>}
                            {{linewidth | lw} <line_width>}
                            {{pointtype | pt} <point_type>}
                            {{pointsize | ps} <point_size>}
                            {palette}
     unset style line
     show style line

     */

}
