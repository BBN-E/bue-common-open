package com.bbn.nlp.clusters;

import com.bbn.bue.common.symbols.Symbol;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public final class ClusterTest {

  private static Clusters clusters;

  @Before
  public void setUp() throws Exception {
    final File sampleFile = new File(ClusterTest.class.getResource("/sample.clusters").getFile());

    clusters = Clusters.from(sampleFile);
  }

  @Test
  public void testBasic() {
    final Optional<Cluster> optBanana = clusters.getClusterForWord(Symbol.from("banana"));
    final Optional<Cluster> optTrain = clusters.getClusterForWord(Symbol.from("train"));
    final Optional<Cluster> fail =
        clusters.getClusterForWord(Symbol.from("sir-not-appearing-in-these-clusters"));

    assertTrue(!fail.isPresent());
    assertTrue(optBanana.isPresent());
    assertTrue(optTrain.isPresent());

    final Cluster train = optTrain.get();
    final Cluster banana = optBanana.get();

    Assert.assertEquals(train.bits(), 5);
    Assert.assertEquals(banana.bits(), 4);

    Assert.assertEquals(train.asSymbol(), Symbol.from("11110"));
    Assert.assertEquals(banana.asSymbol(), Symbol.from("1010"));

    Assert.assertEquals(train.asSymbolTruncatedToNBits(2), Symbol.from("11"));
    Assert.assertEquals(banana.asSymbolTruncatedToNBits(2), Symbol.from("10"));

		/*assertEquals(train.asInteger(), 30);
                assertEquals(banana.asInteger(), 10);

		assertEquals(train.asIntegerTruncatedToNBits(2), 3);
		assertEquals(banana.asIntegerTruncatedToNBits(2), 2);*/

    //	assertEquals(train.asSymbolTruncatedToNBits(ImmutableList.of(2,3,5)),
    //		ImmutableSet.of(Symbol.from("11"), Symbol.from("111"), Symbol.from("11110")));

    assertEquals(ImmutableSet.copyOf(clusters.getWords(train)),
        ImmutableSet.of(Symbol.from("train")));
    assertEquals(ImmutableSet.copyOf(clusters.getWords(train, 2)), ImmutableSet.of(
        Symbol.from("spaceship"), Symbol.from("airplane"), Symbol.from("train"),
        Symbol.from("car")));
  }
}
