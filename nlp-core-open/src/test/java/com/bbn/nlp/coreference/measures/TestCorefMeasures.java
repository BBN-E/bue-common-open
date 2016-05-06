package com.bbn.nlp.coreference.measures;

import com.bbn.bue.common.StringUtils;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Test of coreference methods derived from p. 124-125 of Marta Recasens Potau's PhD thesis
 * "Coreference: Theory Annotation, Resolution, and Evaluation" located at
 * http://stel.ub.edu/cba2010/phd/phd.pdf
 */
@Beta
public final class TestCorefMeasures {

  private static final ImmutableSet<ImmutableSet<Integer>> GROUND_TRUTH = parseClustering(
      "(1) (2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15) (16) (17) (18)"
          + " (19) (20) (21) (22) (23) (24) (25) (26) (27) (28) (29) (30) (31) (32) (33) (34)"
          + " (35) (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47) (48) (49) (50)"
          + " (51) (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)"
          + " (62,63,64,65) (66,67,68) (69,70)");
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_A = parseClustering(
      "(1,2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15) (16) (17) (18)"
          + " (19) (20) (21) (22) (23) (24) (25) (26) (27) (28) (29) (30) (31) (32) (33) (34)"
          + " (35) (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47) (48) (49) (50)"
          + " (51) (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)"
          + " (62,63,64,65) (66,67,68) (69,70)");
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_B = parseClustering(
      "(1,62,63,64,65)"
          + " (2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15)"
          + " (16) (17) (18) (19) (20) (21) (22) (23) (24) (25) (26) (27) (28) (29) (30) (31)"
          + " (32) (33) (34) (35) (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47)"
          + " (48) (49) (50) (51) (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)"
          + " (66,67,68) (69,70)");
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_C = parseClustering(
      "(1) (2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15) (16) (17) (18) (19)" +
          " (20) (21) (22) (23) (24) (25) (26) (27) (28) (29) (30) (31) (32) (33) (34) (35)" +
          " (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47) (48) (49) (50) (51)" +
          " (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)" +
          " (62,63,64,65) (66) (67) (68) (69,70)");
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_D = parseClustering(
      "(1) (2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15) (16) (17) (18) (19)"
          + " (20) (21) (22) (23) (24) (25) (26) (27) (28) (29) (30) (31) (32) (33) (34) (35)"
          + " (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47) (48) (49) (50) (51)"
          + " (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)"
          + " (62,63,64,65,66,67,68) (69,70)");
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_E = parseClustering(
      "(1,62,63) (2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15) (16) (17) (18)"
          + " (19) (20) (21) (22) (23) (24) (25) (26) (27) (28,64,65)"
          + " (29) (30) (31) (32) (33)"
          + " (34) (35) (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47) (48) (49)"
          + " (50) (51) (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)"
          + " (66,67,68) (69,70)");
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_F = parseClustering(
      "(1,62) (2) (3) (4,63)"
          + " (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15) (16) (17)"
          + " (18) (19) (20) (21) (22) (23) (24) (25) (26) (27)"
          + " (28,64) (29) (30) (31) (32) (33)"
          + " (34) (35) (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47) (48) (49)"
          + " (50) (51) (52) (53) (54) (55) (56)"
          + " (57,65) (58) (59) (60) (61) (66,67,68) (69,70)");
  // all singletons
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_G = parseClustering(
      "(1) (62) (63) (64) (65)"
          + " (2) (3) (4) (5) (6) (7) (8) (9) (10) (11) (12) (13) (14) (15)"
          + " (16) (17) (18) (19) (20) (21) (22) (23) (24) (25) (26) (27) (28) (29) (30) (31)"
          + " (32) (33) (34) (35) (36) (37) (38) (39) (40) (41) (42) (43) (44) (45) (46) (47)"
          + " (48) (49) (50) (51) (52) (53) (54) (55) (56) (57) (58) (59) (60) (61)"
          + " (66) (67) (68) (69) (70)");
  // one entity
  private static final ImmutableSet<ImmutableSet<Integer>> SYSTEM_H = parseClustering(
      "(1,62,63,64,65,"
          + "2,3,4,5,6,7,8,9,10,11,12,13,14,15,"
          + "16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,"
          + "32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,"
          + "48,49,50,51,52,53,54,55,56,57,58,59,60,61,"
          + "66,67,68,69,70)");

  @Test
  public void testB3ByElement() {
    final B3Scorer b3Scorer = B3Scorer.createByElementScorer();
    assertEquals(99.28, 100.0 * b3Scorer.score(SYSTEM_A, GROUND_TRUTH).F1(), .01);
    assertEquals(98.84, 100.0 * b3Scorer.score(SYSTEM_B, GROUND_TRUTH).F1(), .01);
    assertEquals(98.55, 100.0 * b3Scorer.score(SYSTEM_C, GROUND_TRUTH).F1(), .01);
    assertEquals(97.49, 100.0 * b3Scorer.score(SYSTEM_D, GROUND_TRUTH).F1(), .01);
    assertEquals(96.66, 100.0 * b3Scorer.score(SYSTEM_E, GROUND_TRUTH).F1(), .01);
    assertEquals(94.99, 100.0 * b3Scorer.score(SYSTEM_F, GROUND_TRUTH).F1(), .01);
    assertEquals(95.52, 100.0 * b3Scorer.score(SYSTEM_G, GROUND_TRUTH).F1(), .01);
    assertEquals(3.61, 100.0 * b3Scorer.score(SYSTEM_H, GROUND_TRUTH).F1(), .01);


  }

  @Test
  public void testBLANC() {
    final BLANCScorer blancScorer = BLANCScorers.getStandardBLANCScorer();
    assertEquals(97.61, 100.0 * blancScorer.score(SYSTEM_A, GROUND_TRUTH).blancScore(), .01);
    assertEquals(91.63, 100.0 * blancScorer.score(SYSTEM_B, GROUND_TRUTH).blancScore(), .01);
    assertEquals(91.15, 100.0 * blancScorer.score(SYSTEM_C, GROUND_TRUTH).blancScore(), .01);
    assertEquals(81.12, 100.0 * blancScorer.score(SYSTEM_D, GROUND_TRUTH).blancScore(), .01);
    assertEquals(79.92, 100.0 * blancScorer.score(SYSTEM_E, GROUND_TRUTH).blancScore(), .01);
    assertEquals(72.12, 100.0 * blancScorer.score(SYSTEM_F, GROUND_TRUTH).blancScore(), .01);
    assertEquals(49.90, 100.0 * blancScorer.score(SYSTEM_G, GROUND_TRUTH).blancScore(), .01);
    assertEquals(0.41, 100.0 * blancScorer.score(SYSTEM_H, GROUND_TRUTH).blancScore(), .01);
  }

  @Test
  public void testMultiBLANC() {
    final BLANCScorer multiBlancScorer = BLANCScorers.getMultiBLANCScorer();
    final ImmutableSet<ImmutableSet<Integer>> reference = parseClustering("(1,2) (3,4,5,6) (7)");
    final ImmutableSet<ImmutableSet<Integer>> system = parseClustering("(1,2,3) (4,5,8) (9)");
    final BLANCResult score = multiBlancScorer.score(system, reference);
    assertEquals(2 / 7.0, score.corefLinkRecall().get(), .0001);
    assertEquals(1 / 3.0, score.corefLinkPrecision().get(), .0001);
    assertEquals(2 / 7.0, score.nonCorefLinkRecall().get(), .0001);
    assertEquals(4 / 15.0, score.nonCorefLinkPrecision().get(), .0001);
    assertEquals(.30769, score.corefLinkF1().get(), .0001);
    assertEquals(.27586, score.nonCorefLinkF1().get(), .0001);
    assertEquals((.30769 + .27586) / 2.0, score.blancScore(), .0001);
  }

  @Test
  public void testMUC() {
    final MUCScorer mucScorer = MUCScorer.create();
    assertEquals(92.31, 100.0 * mucScorer.score(SYSTEM_A, GROUND_TRUTH).get().F1(), .01);
    assertEquals(92.31, 100.0 * mucScorer.score(SYSTEM_B, GROUND_TRUTH).get().F1(), .01);
    assertEquals(80.0, 100.0 * mucScorer.score(SYSTEM_C, GROUND_TRUTH).get().F1(), .01);
    assertEquals(92.31, 100.0 * mucScorer.score(SYSTEM_D, GROUND_TRUTH).get().F1(), .01);
    assertEquals(76.92, 100.0 * mucScorer.score(SYSTEM_E, GROUND_TRUTH).get().F1(), .01);
    assertEquals(46.15, 100.0 * mucScorer.score(SYSTEM_F, GROUND_TRUTH).get().F1(), .01);
    assertFalse(mucScorer.score(SYSTEM_G, GROUND_TRUTH).isPresent());
    assertEquals(16.0, 100.0 * mucScorer.score(SYSTEM_H, GROUND_TRUTH).get().F1(), .01);
  }

  /*@Test
  public void testMentionCEAF() {
    final MentionCEAFScorer ceafScorer = MentionCEAFScorer.create();
    assertEquals(98.57, 100.0 * ceafScorer.score(SYSTEM_A, GROUND_TRUTH).F1(), .01);
    assertEquals(98.57, 100.0 * ceafScorer.score(SYSTEM_B, GROUND_TRUTH).F1(), .01);
    assertEquals(97.14, 100.0 * ceafScorer.score(SYSTEM_C, GROUND_TRUTH).F1(), .01);
    assertEquals(95.71, 100.0 * ceafScorer.score(SYSTEM_D, GROUND_TRUTH).F1(), .01);
    assertEquals(95.71, 100.0 * ceafScorer.score(SYSTEM_E, GROUND_TRUTH).F1(), .01);
    assertEquals(94.29, 100.0 * ceafScorer.score(SYSTEM_F, GROUND_TRUTH).F1(), .01);
    assertEquals(91.43, 100.0 * ceafScorer.score(SYSTEM_G, GROUND_TRUTH).F1(), .01);
    assertEquals(5.71, 100.0 * ceafScorer.score(SYSTEM_H, GROUND_TRUTH).F1(), .01);
  }*/

  private static final ImmutableSet<ImmutableSet<Integer>> parseClustering(String s) {
    final ImmutableSet.Builder<ImmutableSet<Integer>> ret = ImmutableSet.builder();

    // strip parens
    for (final String inParens : StringUtils.onSpaces().split(s)) {
      final String withinParens = inParens.substring(1, inParens.length() - 1);
      final ImmutableSet.Builder<Integer> cluster = ImmutableSet.builder();
      for (final String number : StringUtils.onCommas().split(withinParens)) {

        // strip parens before parsing
        cluster.add(Integer.parseInt(number));
      }
      ret.add(cluster.build());
    }

    return ret.build();
  }
}
