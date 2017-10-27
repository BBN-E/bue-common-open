package com.bbn.bue.gnuplot;

import com.bbn.bue.common.AbstractParameterizedModule;
import com.bbn.bue.common.ModuleFromParameter;
import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.gnuplot.outputformats.Png;

import com.google.common.annotations.Beta;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.inject.Provides;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Beta
public final class GnuPlotRenderer {
  private static final Logger log = LoggerFactory.getLogger(GnuPlotRenderer.class);
  private final File executable;
  private final OutputFormat outputFormat;

  @Inject
  private GnuPlotRenderer(@GnuPlotBinaryP final File pathToGnuPlot,
      final OutputFormat outputFormat) {
    checkArgument(pathToGnuPlot.isFile());
    this.executable = checkNotNull(pathToGnuPlot);
    this.outputFormat = checkNotNull(outputFormat);
  }

  public static GnuPlotRenderer createForGnuPlotExecutable(File gnuPlotExecutable) {
    return new GnuPlotRenderer(gnuPlotExecutable, Png.builder().build());
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

  private static void trueMain(String[] argv) throws IOException, InterruptedException {
    final File gnuPlotExecutable = new File(argv[0]);
    final File dirRoot = new File(argv[1]);
    walk(GnuPlotRenderer.createForGnuPlotExecutable(gnuPlotExecutable), dirRoot);
  }

  private static final ImmutableSet<String> GNUPLOT_EXTENSIONS =
      ImmutableSet.of("gnuplot", "gnuPlot");

  private static void walk(GnuPlotRenderer renderer, File curDir)
      throws IOException, InterruptedException {
    checkArgument(curDir.isDirectory());
    for (final File f : curDir.listFiles()) {
      if (f.isDirectory()) {
        walk(renderer, f);
      } else if (f.isFile() && GNUPLOT_EXTENSIONS.contains(Files.getFileExtension(f.getName()))) {
        renderer.render(f, FileUtils.swapExtension(f, "png"));
      }
    }
  }

  @Deprecated
  public void render(File gnuPlotCommandFile, File pngOutputFile)
      throws IOException, InterruptedException {
    throw new UnsupportedOperationException("Revise you code to use PlotBundles");
  }

  public void renderTo(GnuPlottable plot, File pngOutputFile) throws IOException {
    renderTo(plot.toPlotBundle(), pngOutputFile);
  }

  public void renderTo(PlotBundle plotBundle, File pngOutputFile) throws IOException {
    final File tmpDir = Files.createTempDir();
    tmpDir.deleteOnExit();

    log.info("Rendering plot to {}", pngOutputFile);

    final StringBuilder sb = new StringBuilder();
    outputFormat.appendGnuPlotCommands(sb);
    sb.append("set output \"" + pngOutputFile.getAbsolutePath() + "\"\n");
    sb.append(plotBundle.commandsWritingDataTo(tmpDir));
    final String commands = sb.toString();

    final ProcessBuilder processBuilder = new ProcessBuilder(executable.getAbsolutePath());
    processBuilder.directory(tmpDir);
    processBuilder.redirectErrorStream();

    final Process process = processBuilder.start();

    final OutputStream commandStream = process.getOutputStream();
    final Writer out = new OutputStreamWriter(commandStream, Charsets.UTF_8);

    out.write(commands);
    out.flush();
    out.close();

    final InputStream inStream = process.getInputStream();
    final BufferedReader inReader =
        new BufferedReader(new InputStreamReader(inStream, Charsets.UTF_8));
    String line;
    while ((line = inReader.readLine()) != null) {
      log.error(line);
    }
    final String output = CharStreams.toString(inReader);
    final int exitCode;
    try {
      exitCode = process.waitFor();
    } catch (InterruptedException e) {
      throw new RuntimeException("GnuPlot rendering interrupted", e);
    }

    if (exitCode != 0) {
      throw new RuntimeException("GnuPlot rendering failed with exit code " + exitCode
          + ". Commands were " + commands + "\nOutput was: \n"
          + output);
    }
  }

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface GnuPlotBinaryP {

    String param = "gnuplot.binary";
  }

  public static final class FromParamsModule extends AbstractParameterizedModule {

    public FromParamsModule(final Parameters parameters) {
      super(parameters);
    }

    @Override
    public void configure() {
      install(ModuleFromParameter.forParameter("gnuplot.outputFormatModule").withDefault(Png.class)
          .extractFrom(params()));
    }

    @Provides
    @GnuPlotBinaryP
    File gnuplotBinaryPath() {
      return params().getExistingFile(GnuPlotBinaryP.param);
    }

  }
}
