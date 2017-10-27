package com.bbn.bue.gnuplot;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A PlotBundle is rendering commands together with data which have not yet been tied to particular
 * files. Thi sis needed because the command file needs to refer to the data files by path, but
 * cannot do so until those data file have been written out.
 */
public final class PlotBundle {

  private final ImmutableSet<DatafileReference> datafileReferences;
  private final ImmutableList<Object> commandComponents;

  private PlotBundle(final Iterable<Object> commandComponents,
      final Iterable<DatafileReference> datafileReferences) {
    this.commandComponents = ImmutableList.copyOf(commandComponents);
    this.datafileReferences = ImmutableSet.copyOf(datafileReferences);
  }

  /**
   * Writes the plot data to the indicated directory and returns the GnuPlot commands for the plot
   * as a string. These commands will reference the written data files.
   */
  public String commandsWritingDataTo(File dataDirectory) throws IOException {
    final Map<DatafileReference, File> refsToFiles = Maps.newHashMap();
    for (final DatafileReference datafileReference : datafileReferences) {
      final File randomFile = File.createTempFile("plotBundle", ".dat", dataDirectory);
      randomFile.deleteOnExit();
      refsToFiles.put(datafileReference, randomFile);
      Files.asCharSink(randomFile, Charsets.UTF_8).write(datafileReference.data);
    }

    final StringBuilder ret = new StringBuilder();
    for (final Object commandComponent : commandComponents) {
      if (commandComponent instanceof String) {
        ret.append(commandComponent);
      } else if (commandComponent instanceof DatafileReference) {
        final File file = refsToFiles.get(commandComponent);
        if (file != null) {
          ret.append(file.getAbsolutePath());
        } else {
          throw new RuntimeException("PlotBundle references an unknown data source");
        }
      } else {
        throw new RuntimeException("Unknown command component " + commandComponent);
      }
    }
    return ret.toString();
  }

  ImmutableSet<DatafileReference> datafileReferences() {
    return datafileReferences;
  }

  ImmutableList<Object> commandComponents() {
    return commandComponents;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class DatafileReference {

    private final String data;

    private DatafileReference(final String data) {
      this.data = checkNotNull(data);
    }
  }

  public static final class Builder {

    private final ImmutableList.Builder<Object> commandElements = ImmutableList.builder();
    private final ImmutableSet.Builder<DatafileReference> dataFileReferences =
        ImmutableSet.builder();

    // we use this buffer to collapse together multiple successive string appends
    private StringBuilder sbBuffer = new StringBuilder();

    public Builder append(String s) {
      sbBuffer.append(s);
      return this;
    }

    public Builder append(int x) {
      append(Integer.toString(x));
      return this;
    }

    public Builder append(double x) {
      append(Double.toString(x));
      return this;
    }

    public Builder appendData(String data) {
      return appendData(new DatafileReference(data));
    }

    public DatafileReference getReferenceFor(String data) {
      return new DatafileReference(data);
    }

    public Builder appendData(DatafileReference dataRef) {
      clearStringBuffer();

      dataFileReferences.add(dataRef);
      commandElements.add(dataRef);
      return this;
    }

    private void clearStringBuffer() {
      final String bufferContents = sbBuffer.toString();
      if (!bufferContents.isEmpty()) {
        commandElements.add(bufferContents);
      }
      sbBuffer = new StringBuilder();
    }

    public PlotBundle build() {
      commandElements.add(sbBuffer.toString());
      return new PlotBundle(commandElements.build(), dataFileReferences.build());
    }
  }
}
