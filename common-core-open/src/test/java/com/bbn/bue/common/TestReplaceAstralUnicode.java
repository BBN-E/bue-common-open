package com.bbn.bue.common;

import com.bbn.bue.common.files.FileUtils;
import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;


public class TestReplaceAstralUnicode {

  @Rule
  public TemporaryFolder inPlaceFolder = new TemporaryFolder();

  // \uD83E\uDDC0 is CHEESE WEDGE, the emoji with the highest code point (as of 2017)
  private static final String ASTRAL_STRING = "Hello world \uD83E\uDDC0\nfoo\uD83E\uDDC0foo";
  public static final String DE_ASTRALIZE_STRING = "Hello world \uFFFD\nfoo\uFFFDfoo";

  @Test
  public void testInPlace() throws Exception {
    inPlaceFolder.create();
    final File root = inPlaceFolder.getRoot();
    final File badFile = new File(root, "badFile.txt");
    Files.asCharSink(badFile, Charsets.UTF_8).write(ASTRAL_STRING);
    // this is to make sure offsets are unaltered
    final String writtenString = Files.asCharSource(badFile, Charsets.UTF_8).read();
    final long oldLength = writtenString.codePointCount(0, writtenString.length());

    final ImmutableMap<Symbol, File> map = ImmutableMap.of(Symbol.from("bad"), badFile);
    final File mapFile = new File(root, "map.txt");
    FileUtils.writeSymbolToFileMap(map, Files.asCharSink(mapFile, Charsets.UTF_8));

    final Parameters params = Parameters.builder()
        .set(ReplaceAstralUnicodeCodepoints.IN_PLACE_PARAM, "true")
        .set(ReplaceAstralUnicodeCodepoints.INPUT_MAP_PARAM, mapFile.getAbsolutePath())
        .build();

    final File paramsFile = new File(root, "params.params");
    Files.asCharSink(paramsFile, Charsets.UTF_8).write(params.dump());

    ReplaceAstralUnicodeCodepoints.main(new String[]{paramsFile.getAbsolutePath()});
    final String rereadString = Files.asCharSource(badFile, Charsets.UTF_8).read();
    assertEquals(DE_ASTRALIZE_STRING, rereadString);
    assertEquals(oldLength, rereadString.codePointCount(0, rereadString.length()));
  }

  @Rule
  public TemporaryFolder nonInPlaceFolder = new TemporaryFolder();

  @Test
  public void testNonInPlace() throws Exception {
    nonInPlaceFolder.create();
    final File root = nonInPlaceFolder.getRoot();
    final File inputDir = new File(root, "inputDir");
    final File foo = new File(inputDir, "foo");
    final File outputDir = new File(root, "output");
    //noinspection ResultOfMethodCallIgnored
    foo.mkdirs();
    // we use two test files at different nesting levels to test directory structure
    // is preserved
    final File meep = new File(inputDir, "meep.txt");
    Files.asCharSink(meep, Charsets.UTF_8).write(ASTRAL_STRING);
    final File bar = new File(foo, "bar.txt");
    Files.asCharSink(bar, Charsets.UTF_8).write(ASTRAL_STRING);

    final File mapFile = new File(root, "map.txt");
    FileUtils.writeSymbolToFileMap(ImmutableMap.of(Symbol.from("meep"), meep,
        Symbol.from("bar"), bar), Files.asCharSink(mapFile, Charsets.UTF_8));

    final Parameters params = Parameters.builder()
        .set(ReplaceAstralUnicodeCodepoints.INPUT_MAP_PARAM, mapFile.getAbsolutePath())
        .set(ReplaceAstralUnicodeCodepoints.OUTPUT_DIR_PARAM, outputDir.getAbsolutePath())
        .set(ReplaceAstralUnicodeCodepoints.BASE_PATH_PARAM, inputDir.getAbsolutePath())
        .set(ReplaceAstralUnicodeCodepoints.OUTPUT_MAP_PARAM,
            new File(root, "outputMap.txt").getAbsolutePath())
        .build();

    final File paramsFile = new File(root, "params.txt");
    Files.asCharSink(paramsFile, Charsets.UTF_8).write(params.dump());

    ReplaceAstralUnicodeCodepoints.main(new String[]{paramsFile.getAbsolutePath()});
    assertEquals(DE_ASTRALIZE_STRING, Files.asCharSource(new File(outputDir, "meep.txt"),
        Charsets.UTF_8).read());
    assertEquals(DE_ASTRALIZE_STRING, Files.asCharSource(new File(new File(outputDir, "foo"),
            "bar.txt"),
        Charsets.UTF_8).read());
  }
}
