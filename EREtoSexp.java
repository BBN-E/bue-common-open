package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.serif.apf.APFDocument;
import com.bbn.serif.apf.APFToSexpConverter;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class EREtoSexp {
  private EREtoSexp() {
    throw new UnsupportedOperationException();
  }

  private static final Logger log = LoggerFactory.getLogger(EREtoSexp.class);

  public static void main(final String[] argv) throws IOException {
    try {
      trueMain(argv[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static void trueMain(final String paramFile) throws IOException {
    final Parameters params = Parameters.loadSerifStyle(new File(paramFile));

    final ImmutableMap<String, String> originalTextMap =
        originalTextMap(params.getExistingFile("ere.sourceFilelist"));


    final ERELoader ereLoader = ERELoader.from(params);
    final EREtoAPF ereToApf = EREtoAPF.from(params);

    final ImmutableList<File> filelist =
        FileUtils.loadFileList(params.getExistingFile("ere.xmlFilelist"));

    final APFToSexpConverter converter = APFToSexpConverter.createForCharacterOffsets()
        .withOffsetAdjusment(params.getInteger("ere.offsetAdjust")).build();
    final StringBuilder sb = new StringBuilder();

    for (final File inFile : filelist) {
      final EREDocument ereDoc = ereLoader.loadFrom(inFile);
      System.out.println("Loaded ERE document + " + ereDoc.getDocId());
      final APFDocument apfDoc = ereToApf.toAPFDocument(ereDoc);
      System.out.println("... converted ERE to APF");

      sb.append(converter.toSexp(apfDoc, originalTextMap.get(ereDoc.getDocId())));
      sb.append("\n");
    }

    converter.finish();

    final CharSink sink =
        Files.asCharSink(params.getCreatableFile("sexp.filename"), Charsets.UTF_8);
    sink.write(sb.toString());

  }

  private static ImmutableMap<String, String> originalTextMap(final File sourceFileList)
      throws IOException {
    final ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();

    for (final File f : FileUtils.loadFileList(sourceFileList)) {
      final CharSource source = Files.asCharSource(f, Charsets.UTF_8);

      final String docId;
      final String firstLine = source.readFirstLine();
      if (firstLine != null && (firstLine.toLowerCase().indexOf("doc id=") >= 0)) {
        docId = getDocid(firstLine);
      } else {
        final String fileId = f.getName();
        docId = fileId.substring(0, fileId.indexOf("."));
      }

      ret.put(docId, source.read());
    }

    return ret.build();
  }

  private static String getDocid(final String line) {
    final int i1 = line.indexOf("\"", line.indexOf(" id="))+1;
    final int i2 = line.indexOf("\"", i1);
    return line.substring(i1, i2);

  }

}


