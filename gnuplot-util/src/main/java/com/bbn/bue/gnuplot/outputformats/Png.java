package com.bbn.bue.gnuplot.outputformats;

import com.bbn.bue.common.AbstractParameterizedModule;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.gnuplot.OutputFormat;

import com.google.inject.Provides;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;

import static com.google.common.base.Preconditions.checkArgument;

public final class Png implements OutputFormat {

  private final int xResolution;
  private final int yResolution;

  @Inject
  private Png(@XResolutionP int xResolution,
      @YResolutionP int yResolution) {
    checkArgument(xResolution > 0);
    this.xResolution = xResolution;
    checkArgument(yResolution > 0);
    this.yResolution = yResolution;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public void appendGnuPlotCommands(StringBuilder sb) {
    // dashed enables dashed lines, but other code is putting
    // dashed lines in places we don't want, so I've disabled
    // it for now ~ rgabbard
//    sb.append("set terminal pngcairo dashed size ")
    sb.append("set terminal pngcairo dashed size ")
        .append(xResolution).append(",")
        .append(yResolution).append("\n");

  }

  public static class Builder {

    private int xResolution = 1024;
    private int yResolution = 768;

    private Builder() {
    }

    public Builder setResolution(int x, int y) {
      xResolution = x;
      yResolution = y;
      return this;
    }

    public Png build() {
      return new Png(xResolution, yResolution);
    }
  }

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface XResolutionP {

    String param = "gnuplot.output.defaultXResolution";
    int defaultVal = 1024;
  }

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface YResolutionP {

    String param = "gnuplot.output.defaultYResolution";
    int defaultVal = 768;
  }

  public static final class FromParamsModule extends AbstractParameterizedModule {

    protected FromParamsModule(final Parameters parameters) {
      super(parameters);
    }

    @Override
    public void configure() {
      bind(OutputFormat.class).to(Png.class);
    }

    @Provides
    @XResolutionP
    int xResolution() {
      return params().getOptionalInteger(XResolutionP.param)
          .or(XResolutionP.defaultVal);
    }

    @Provides
    @YResolutionP
    int yResolution() {
      return params().getOptionalInteger(YResolutionP.param)
          .or(YResolutionP.defaultVal);
    }
  }
}
