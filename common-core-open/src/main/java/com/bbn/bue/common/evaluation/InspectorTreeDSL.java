package com.bbn.bue.common.evaluation;

import com.bbn.bue.common.Inspector;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.reflect.TypeToken;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Inspector trees are a convenient way to write scorers (and possibly other corpus observers) which
 * compute several related things from one input. This class, meant to be statically imported,
 * defines a simple language for building inspector trees.
 *
 * Inspector tree nodes have a single input and multiple consumers.  They receive their input,
 * possibly act on it, and either pass it unchanged or in some transformed form to their consumers.
 *
 * {@link InspectionNode}s pass their input to their output unchanged but may apply zero or more
 * inspectors as the data passed through.
 *
 * {@link TransformNode}s do not inspect their input, but they do apply some transformation to it
 * before passing it on to their consumers.
 *
 * To build an inspector tree, first determine what the original input type to the tree will be and
 * create a corresponding {@link TypeToken}(s).  Then use the type tokens to create input nodes
 * using either {@link #input(TypeToken)} or {@link #pairedInput(TypeToken)}, depending on whether
 * you are observing single objects or pairs of objects (e.g. in a scorer, where you have system
 * output and a gold standard).  This will be the root of the inspection tree which you will feed
 * the data to inspect to.  It will be an {@link InspectionNode} without any inspectors.
 *
 * To add a transformation node, use use {@link #transformed(InspectorTreeNode, Function)} or {@link
 * #transformBoth(InspectorTreeNode, Function)}. To add an observation node, use {@link
 * #inspect(InspectorTreeNode)} like this:
 * <pre>
 *    inspect(myInputNode).with(someInspector);
 *  </pre>
 *
 * An example:
 * <pre>
 *    TypeToken&lt;Integer&gt; inputIsInteger = new TypeToken&lt;Integer&gt; () {}
 *    InspectionNode&lt;Integer&gt; input = input(inputIsInteger);
 *    inspect(input).with(printFunction)
 *    InspectorTreeNode&lt;Integer&gt; plusTwo = transformed(input, plusTwoFunction);
 *    inspect(plusTwo).with(printFunction);
 *    inspect(plusTwo).with(emailToFredFunction);
 *
 *    for (int x : myInts) {
 *       input.inspect(x);
 *    }
 *    input.finish();
 *
 *  </pre>
 *
 * Users beware - this is a very rough draft and is likely to change in the future without warning.
 */
@Beta
public final class InspectorTreeDSL {

  public static <T> InspectionNode<T> input(TypeToken<T> typeToken) {
    // this method is just syntactic sugar
    return new InspectionNode<T>();
  }

  public static <T> InspectionNode<EvalPair<T, T>> pairedInput(TypeToken<T> typeToken) {
    // this method is just syntactic sugar
    return new InspectionNode<>();
  }

  public static <T, V> InspectionNode<EvalPair<T, V>> pairedInput(TypeToken<T> leftTypeToken,
      TypeToken<V> rightTypeToken) {
    // this method is just syntactic sugar
    return new InspectionNode<>();
  }

  public static <InT, OutT> InspectorTreeNode<OutT> transformed(InspectorTreeNode<InT> inputNode,
      Function<? super InT, ? extends OutT> func) {
    final TransformNode<InT, OutT> ret =
        new TransformNode<>(func);
    inputNode.registerConsumer(ret);
    return ret;
  }

  public static <KeyT extends F, ValT extends F, F, T> InspectorTreeNode<EvalPair<T, T>> transformBoth(
      InspectorTreeNode<EvalPair<KeyT, ValT>> inputNode,
      Function<F, T> func) {
    final TransformNode<EvalPair<KeyT, ValT>, EvalPair<T, T>> ret =
        new TransformNode<>(EvalPair.functionOnBoth(func));
    inputNode.registerConsumer(ret);
    return ret;
  }

  public static <KeyT extends F, ValT, F, T> InspectorTreeNode<EvalPair<T, ValT>> transformLeft(
      InspectorTreeNode<EvalPair<KeyT, ValT>> inputNode,
      Function<F, T> func) {
    final TransformNode<EvalPair<KeyT, ValT>, EvalPair<T, ValT>> ret =
        new TransformNode<>(EvalPair.functionsOnBoth(func, Functions.<ValT>identity()));
    inputNode.registerConsumer(ret);
    return ret;
  }

  public static <KeyT, ValT extends F, F, T> InspectorTreeNode<EvalPair<KeyT, T>> transformRight(
      InspectorTreeNode<EvalPair<KeyT, ValT>> inputNode,
      Function<F, T> func) {
    final TransformNode<EvalPair<KeyT, ValT>, EvalPair<KeyT, T>> ret =
        new TransformNode<>(EvalPair.functionsOnBoth(Functions.<KeyT>identity(), func));
    inputNode.registerConsumer(ret);
    return ret;
  }

  public static <InT> InspectionBuilder<InT> inspect(InspectorTreeNode<InT> inputNode) {
    return new InspectionBuilder<>(inputNode);
  }

  public static final class InspectionBuilder<InT> {

    final InspectorTreeNode<InT> inputNode;

    private InspectionBuilder(final InspectorTreeNode<InT> inputNode) {
      this.inputNode = checkNotNull(inputNode);
    }

    public InspectionNode<InT> with(Inspector<? super InT> inspector) {
      final InspectionNode<InT> ret = new InspectionNode<>();
      inputNode.registerConsumer(ret);
      ret.registerConsumer(inspector);
      return ret;
    }

    public InspectionNode<InT> with(Iterable<? extends Inspector<? super InT>> inspectors) {
      final InspectionNode<InT> ret = new InspectionNode<>();
      inputNode.registerConsumer(ret);
      for (final Inspector<? super InT> inspector : inspectors) {
        ret.registerConsumer(inspector);
      }
      return ret;
    }
  }
}
