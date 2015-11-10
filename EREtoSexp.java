package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.nlp.corpora.apf.EDTOffsetMapper;
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


    final ImmutableMap<String, OffsetInfo> offsetInfo = offsetInfoFromSource(params);
    //for(final Map.Entry<String, OffsetInfo> entry : offsetInfo.entrySet()) {
    //  System.out.println("== " + entry.getKey() + " ==");
    //  System.out.println(entry.getValue().toString());
    //}


    final ERELoader ereLoader = ERELoader.from(params);
    final EREtoAPF ereToApf = EREtoAPF.from(params);

    //System.out.println(ereToApf.getEREtoACETypeMapper().toString());    // checks we have loaded resource mappings


    final ImmutableList<String> filelist = Files.asCharSource(params.getExistingFile("ere.xmlFilelist"), Charsets.UTF_8).readLines();

    final APFToSexpConverter converter = APFToSexpConverter.create();
    final StringBuilder sb = new StringBuilder();

    for(final String infilename : filelist) {
      final EREDocument ereDoc = ereLoader.loadFrom(new File(infilename));
      System.out.println("Loaded ERE document + " + ereDoc.getDocId());
      final APFDocument apfDoc = ereToApf.toAPFDocument(ereDoc);
      System.out.println("... converted ERE to APF");

      final OffsetInfo docOffset = offsetInfo.get(ereDoc.getDocId());

      sb.append(converter.toSexp(apfDoc, docOffset));
      sb.append("\n");
    }

    final CharSink sink =
        Files.asCharSink(params.getCreatableFile("sexp.filename"), Charsets.UTF_8);
    sink.write(sb.toString());
  }

  private static ImmutableMap<String, OffsetInfo> offsetInfoFromSource(final Parameters params) throws IOException {
    final ImmutableMap.Builder<String, OffsetInfo> ret = ImmutableMap.builder();

    final ImmutableList<String> filelist = Files.asCharSource(params.getExistingFile("ere.sourceFilelist"), Charsets.UTF_8).readLines();

    final int offsetAdjust = params.getOptionalInteger("ere.offsetAdjust").or(0);

    final EDTOffsetMapper edtOffsetMapper =
        EDTOffsetMapper.createWithOffsetAdjustment(offsetAdjust);
    for(final String filename : filelist) {
      final CharSource source = Files.asCharSource(new File(filename), Charsets.UTF_8);

      final String docId;
      final String firstLine = source.readFirstLine();
      if (firstLine != null && (firstLine.toLowerCase().indexOf("doc id=") >= 0)) {
        docId = getDocid(firstLine);
      } else {
        final String fileId = filename.substring(filename.lastIndexOf("/")+1);
        docId = fileId.substring(0, fileId.indexOf("."));
      }

      ret.put(docId, edtOffsetMapper.getOffsetInfoFrom(source));
    }

    return ret.build();

  }

  private static String getDocid(final String line) {
    final int i1 = line.indexOf("\"", line.indexOf(" id="))+1;
    final int i2 = line.indexOf("\"", i1);
    return line.substring(i1, i2);

  }

}


