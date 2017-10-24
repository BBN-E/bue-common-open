package com.bbn.runjobs;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.graphviz.DotRenderer;
import com.bbn.bue.graphviz.Edge;
import com.bbn.bue.graphviz.Graph;
import com.bbn.bue.graphviz.Node;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class GraphRunJobsDependencies {

  private static final Logger log = LoggerFactory.getLogger(GraphRunJobsDependencies.class);

  private static final String USAGE = "usage: GraphRunJobsDependencies dependenciesFile\n"
      + "Produces a visualization of runjobs dependencies.\n"
      + "dependenciesFile is the file resulting from running a runjobs \n"
      + "\tscript with -dump_graph_to=/path/to/dependenciesFile\n"
      + "Output is written to dependenciesFile.pdf\n"
      + "dot (from GraphViz) must be on your system path.\n";

  private GraphRunJobsDependencies() {
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

  private static void trueMain(String[] argv) throws IOException, InterruptedException {
    if (argv.length == 0) {
      System.err.println(USAGE);
      System.exit(1);
    }
    final File dependenciesFile = new File(argv[0]);
    final ImmutableMultimap<String, String> dependerIDToDependeeID =
        FileUtils.loadStringMultimap(Files.asCharSource(dependenciesFile, Charsets.UTF_8));

    final ImmutableMap<String, Node> jobIDsToNodes = createNodes(dependerIDToDependeeID);
    final Graph.Builder graphBuilder =
        Graph.createDirected(dependenciesFile.getName()).rankLeftToRight();
    graphBuilder.addNodes(jobIDsToNodes.values());
    for (final Map.Entry<String, String> e : dependerIDToDependeeID.entries()) {
      final String dependerID = e.getKey();
      final String dependeeID = e.getValue();
      graphBuilder.addEdge(Edge.fromTo(jobIDsToNodes.get(dependerID),
          jobIDsToNodes.get(dependeeID)).build());
    }
    final Optional<File> dotExecutable = findExecutableOnSystemPath("dot");
    if (dotExecutable.isPresent()) {
      final File outputFile = new File(dependenciesFile.getAbsolutePath() + ".pdf");
      DotRenderer.createForDotExecutable(dotExecutable.get()).renderToFile(graphBuilder.build(),
          outputFile);
      log.info("Rendered {} to {}", dependenciesFile, outputFile);
      log.info("TIP: the produces PDFs seem to kill Acrobat Reader and Firefox, but Chrome can "
          + "handle them fine (on Windows, at least)");
    } else {
      log.error("Could not find dot executable on system path");
      System.exit(1);
    }
  }

  // warning: this will fail if the user has an escape path separator in a path
  private static Optional<File> findExecutableOnSystemPath(final String executableName) {
    for (final String pathPath : Splitter.on(File.pathSeparator).split(System.getenv("PATH"))) {
      final File probeFile = new File(pathPath, executableName);
      if (probeFile.isFile() && java.nio.file.Files.isExecutable(probeFile.toPath())) {
        return Optional.of(probeFile);
      }
    }
    return Optional.absent();
  }

  private static ImmutableMap<String, Node> createNodes(
      final ImmutableMultimap<String, String> dependerIDToDependeeID) {
    final ImmutableMap.Builder<String, Node> ret = ImmutableMap.builder();
    final ImmutableSet<String> allJobIDs = FluentIterable.from(dependerIDToDependeeID.keySet())
        .append(dependerIDToDependeeID.values()).toSet();
    for (final String jobID : allJobIDs) {
      ret.put(jobID, Node.builderWithRandomName()
          .withShape(Node.Shape.BOX)
          .withLabel(jobID).build());
    }
    return ret.build();
  }
}
