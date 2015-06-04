package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.nlp.corpora.ere.OffsetInfo.OffsetSpan;
import com.bbn.serif.apf.APFDocument;
import com.bbn.serif.apf.APFeventsToTACslotsMapper;
import com.bbn.serif.apf.APFtoSexp;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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

    // for ERE events to TAC slot conversion
    APFeventsToTACslotsMapper.loadFrom(params);
    //

    final ImmutableMap<String, OffsetInfo> offsetInfo = offsetInfoFromSource(params);
    //for(final Map.Entry<String, OffsetInfo> entry : offsetInfo.entrySet()) {
    //  System.out.println("== " + entry.getKey() + " ==");
    //  System.out.println(entry.getValue().toString());
    //}


    final ERELoader ereLoader = ERELoader.from(params);
    final EREtoAPF ereToApf = EREtoAPF.from(params);

    //System.out.println(ereToApf.getEREtoACETypeMapper().toString());    // checks we have loaded resource mappings


    final ImmutableList<String> filelist = Files.asCharSource(params.getExistingFile("ere.xmlFilelist"), Charsets.UTF_8).readLines();

    List<String> sexpLines = Lists.newArrayList();

    for(final String infilename : filelist) {
      final EREDocument ereDoc = ereLoader.loadFrom(new File(infilename));
      System.out.println("Loaded ERE document + " + ereDoc.getDocId());
      final APFDocument apfDoc = ereToApf.toAPFDocument(ereDoc);
      System.out.println("... converted ERE to APF");

      final OffsetInfo docOffset = offsetInfo.get(ereDoc.getDocId());

      sexpLines.addAll(APFtoSexp.docToSexp(apfDoc, docOffset));
      System.out.println("");
    }

    final String outfile = params.getString("sexp.filename");
    PrintWriter writer = new PrintWriter(outfile, "UTF-8");
    writer.write("(\n");
    for(final String outline : sexpLines) {
      writer.write(outline + "\n");
    }
    writer.write("\n");
    writer.write(")\n");
    writer.close();

  }

  private static ImmutableMap<String, OffsetInfo> offsetInfoFromSource(final Parameters params) throws IOException {
    final ImmutableMap.Builder<String, OffsetInfo> ret = ImmutableMap.builder();

    final ImmutableList<String> filelist = Files.asCharSource(params.getExistingFile("ere.sourceFilelist"), Charsets.UTF_8).readLines();
    
    final int offsetAdjust = params.isPresent("ere.offsetAdjust") ? params.getInteger("ere.offsetAdjust") : 0;

    for(final String filename : filelist) {
      OffsetInfo.Builder offsetBuilder = OffsetInfo.builder();

      final ImmutableList<String> lines = Files.asCharSource(new File(filename), Charsets.UTF_8).readLines();
      
      String docId = filename.substring(filename.lastIndexOf("/")+1);
      docId = docId.substring(0, docId.indexOf("."));
      
      //final String docId = getDocid(lines.get(0));
      

      List<Integer> chars = Lists.newArrayList();
      List<Integer> tags = Lists.newArrayList();
      for(final String line : lines) {
        chars.add(line.length()+1);
        tags.add(tagLength(line));
      }

      int runningCharLen = 0;
      int runningTagLen = 0;
      //System.out.println("== " + docId + " ==");
      for(int index=0; index<lines.size(); index++) {
        final String line = lines.get(index);

        //int transformedOffset = runningCharLen - runningTagLen;

        if(tags.get(index) < line.length()) {   // there is some xml tags and some text spans in this line
          //System.out.print("*");

          //OffsetSpan.Builder spanBuilder = OffsetSpan.builder(runningCharLen, runningCharLen+line.length()).
          //    withOffset(Optional.of(transformedOffset - runningCharLen));
          //offsetBuilder.withSpan(spanBuilder.build());

          ///////
          int i1 = 0;
          int i2 = 0;
          int l = 0;
          while(i2<line.length() && line.indexOf("<", i2)!=-1) {
            i1 = line.indexOf("<", i2);

            if(i2==0 && i1>0) {
              offsetBuilder.withSpan( OffsetSpan.builder(runningCharLen, runningCharLen + i1-1).
                  withOffset(Optional.of(-1*(runningTagLen+offsetAdjust))).build() );
            }
            else {
              if(i1 > i2) {
                offsetBuilder.withSpan( OffsetSpan.builder(runningCharLen+i2, runningCharLen + i1-1).
                    withOffset(Optional.of(-1*(runningTagLen+l+offsetAdjust))).build() );
              }
            }

            if(line.indexOf(">", i1)!=-1) {
              i2 = line.indexOf(">", i1)+1;
              if(i1!=-1 && i2!=-1) {
                l += (i2 - i1);
              }
            }
          }
          if(i2<line.length()) {
            offsetBuilder.withSpan( OffsetSpan.builder(runningCharLen+i2, runningCharLen + line.length()-1).
                withOffset(Optional.of(-1*(runningTagLen+l+offsetAdjust))).build() );
          }


        }
        //System.out.println((index+1) + ": " + runningCharLen + " " + runningTagLen + " " + transformedOffset);

        runningCharLen += chars.get(index);
        runningTagLen += tags.get(index);

      }

      ret.put(docId, offsetBuilder.build());
    }

    return ret.build();

  }

  private static String getDocid(final String line) {
    final int i1 = line.indexOf("\"", line.indexOf(" id="))+1;
    final int i2 = line.indexOf("\"", i1);
    return line.substring(i1, i2);

  }

  private static int tagLength(final String line) {
    int l = 0;

    int i1 = 0;
    int i2 = 0;
    while(line.indexOf("<", i2)!=-1) {
      i1 = line.indexOf("<", i2);
      i2 = line.indexOf(">", i1);
      if(i1!=-1 && i2!=-1) {
        l += (i2 - i1 + 1);
      }
    }

    return l;
  }
}


