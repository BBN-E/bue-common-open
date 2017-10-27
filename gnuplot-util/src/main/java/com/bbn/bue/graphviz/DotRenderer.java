package com.bbn.bue.graphviz;

import com.bbn.bue.common.AbstractParameterizedModule;
import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Inject;
import javax.inject.Qualifier;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DotRenderer {

  private static final Logger log = LoggerFactory.getLogger(DotRenderer.class);

  private final File dotBinary;

  @Inject
  private DotRenderer(@DotBinaryP File dotBinary) {
    checkArgument(dotBinary.isFile(), "Invalid dot binary {}", dotBinary);
    this.dotBinary = checkNotNull(dotBinary);
  }

  public static DotRenderer createForDotExecutable(File dotBinary) {
    return new DotRenderer(dotBinary);
  }

  public void renderToFile(Graph graph, File outputFile) throws IOException, InterruptedException {
    final File dotCommands = File.createTempFile("dotRenderer", ".dot");
    //dotCommands.deleteOnExit();;
    Files.asCharSink(dotCommands, Charsets.UTF_8).write(graph.toDot());
    renderToFile(dotCommands, outputFile);
  }

  public void renderToFile(File dotCommandFile, File outputFile)
      throws IOException, InterruptedException {
    outputFile.getParentFile().mkdirs();

    log.info("Rendering {} to {}", dotCommandFile, outputFile);

    final ImmutableList<String> commands = ImmutableList.of(dotBinary.getAbsolutePath(),
        dotCommandFile.getAbsolutePath(), "-Tpdf", "-o" + outputFile.getAbsolutePath());
    final ProcessBuilder processBuilder = new ProcessBuilder(commands);
    processBuilder.directory(outputFile.getParentFile());
    processBuilder.redirectErrorStream(true);

    final Process process = processBuilder.start();

    final InputStream inStream = process.getInputStream();
    final BufferedReader inReader =
        new BufferedReader(new InputStreamReader(inStream, Charsets.UTF_8));
    String line;
    while ((line = inReader.readLine()) != null) {
      log.error(line);
    }
    final String output = CharStreams.toString(inReader);
    int exitCode = process.waitFor();

    if (exitCode != 0) {
      throw new RuntimeException("Dot rendering failed with exit code " + exitCode
          + ". Commands were " + commands + "\nOutput was: \n"
          + output);
    }
  }

  @Qualifier
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @interface DotBinaryP {

    String param = "graphviz.dotBinary";
  }

  public static final class FromParamsModule extends AbstractParameterizedModule {

    public FromParamsModule(final Parameters parameters) {
      super(parameters);
    }

    @Override
    public void configure() {

    }

    @Provides
    @DotBinaryP
    File dotBinary() {
      return params().getExistingFile(DotBinaryP.param);
    }
  }
}
