package com.bbn.nlp.corenlp;

import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.nlp.ConstituentNode;
import com.bbn.nlp.parsing.HeadFinder;

import com.google.common.annotations.Beta;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Represents a node and data from the StanfordParser, at least from basic XML output. The parent is
 * default Optional.absent() and for child Y of node X, can only be set by the builderForNonTerminal
 * of X.
 */
@Beta
public final class CoreNLPParseNode
        implements ConstituentNode<CoreNLPParseNode, CoreNLPParseNodeData> {

    private final Symbol tag;
    private final ImmutableList<CoreNLPParseNode> children;
    private Optional<CoreNLPParseNode> parent = Optional.absent();
    private final Optional<CoreNLPParseNode> head;
    private final Optional<CoreNLPParseNodeData> data = Optional.absent();
    private final Optional<OffsetRange<CharOffset>> span;
    private final Optional<CoreNLPToken> token;

    private CoreNLPParseNode(final Symbol tag, final List<CoreNLPParseNode> children,
                             final Optional<CoreNLPParseNode> head, final Optional<CoreNLPToken> token) {
        this.head = checkNotNull(head);
        this.token = checkNotNull(token);
        this.tag = checkNotNull(tag);
        this.children = ImmutableList.copyOf(children);
        if (children.isEmpty()) {
            checkState(children.isEmpty(), "if we have a data we must be a terminal node!");
            if (token.isPresent()) {
                span = Optional.of(token.get().offsets());
            } else {
                span = Optional.absent();
            }
        } else {
            // we have to have a span if we're a non-terminal node, and we're sticking to that.
            Optional<CharOffset> start = Optional.absent();
            for (final CoreNLPParseNode child : children) {
                if (child.span().isPresent()) {
                    start = Optional.of(child.span().get().startInclusive());
                    break;
                }
            }
            Optional<CharOffset> end = Optional.absent();
            for (final CoreNLPParseNode child : ImmutableList.copyOf(children).reverse()) {
                if (child.span.isPresent()) {
                    end = Optional.of(child.span().get().endInclusive());
                    break;
                }
            }
            if (start.isPresent()) {
                span = Optional.of(OffsetRange.charOffsetRange(start.get().asInt(), end.get().asInt()));
            } else {
                span = Optional.absent();
            }
        }
        checkArgument(!span.isPresent() || span.get().startInclusive().asInt() <= span.get().endInclusive().asInt(),
                "Span lengths inconsistent!");
    }

    public static CoreNLPParseNode create(final Symbol tag, final List<CoreNLPParseNode> children,
                                          final Optional<CoreNLPParseNode> head, final Optional<CoreNLPToken> token) {
        return new CoreNLPParseNode(tag, children, head, token);
    }

    @Override
    public Symbol tag() {
        return tag;
    }

    @Override
    public ImmutableList<CoreNLPParseNode> children() {
        return children;
    }

    @Override
    public Optional<CoreNLPParseNodeData> nodeData() {
        return data;
    }

    @Override
    public boolean terminal() {
        return children().isEmpty();
    }

    @Override
    public Optional<CoreNLPParseNode> parent() {
        return parent;
    }

    private void setParent(final CoreNLPParseNode parent) {
        checkState(!this.parent.isPresent(), "Parent node may not be set twice!");
        this.parent = Optional.of(parent);
    }

    @Override
    public Optional<CoreNLPParseNode> immediateHead() {
        if (terminal()) {
            return Optional.absent();
        }
        return head;
    }

    public Optional<CoreNLPToken> token() {
        return token;
    }

    /**
     * Resolves a head down to a terminal node. A terminal node is its own head here.
     */
    public Optional<CoreNLPParseNode> terminalHead() {
        if (terminal()) {
            return Optional.of(this);
        }
        if (immediateHead().isPresent()) {
            return immediateHead().get().terminalHead();
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "CoreNLPParseNode{" +
                "tag=" + tag +
                ", children=" + children +
                ", head=" + head +
                ", data=" + data +
                ", terminal=" + terminal() +
                '}';
    }

    /**
     * If terminal: span of the CoreNLPToken; else: span from the start of the first child to the
     * end of the last.
     */
    public Optional<OffsetRange<CharOffset>> span() {
        return span;
    }

    public static CoreNLPParseNodeBuilder buildForTerminal() {
        return new CoreNLPParseNodeBuilder(null);
    }

    public static CoreNLPParseNodeBuilder builderForNonTerminal(
            final HeadFinder<CoreNLPParseNode> headFinder) {
        return new CoreNLPParseNodeBuilder(headFinder);
    }

    public static class CoreNLPParseNodeBuilder {

        private final HeadFinder<CoreNLPParseNode> headFinder;

        private Symbol tag;
        private List<CoreNLPParseNode> children = Lists.newArrayList();
        private Optional<String> text = Optional.absent();
        private Optional<CoreNLPToken> token = Optional.absent();

        private CoreNLPParseNodeBuilder(final HeadFinder<CoreNLPParseNode> headFinder) {
            this.headFinder = headFinder;
        }

        public CoreNLPParseNodeBuilder withTag(Symbol tag) {
            this.tag = tag;
            return this;
        }

        public CoreNLPParseNodeBuilder addChildren(final List<CoreNLPParseNode> children) {
            this.children.addAll(checkNotNull(children));
            return this;
        }

        public CoreNLPParseNodeBuilder addChildren(final CoreNLPParseNode... children) {
            for (final CoreNLPParseNode child : children) {
                this.children.add(checkNotNull(child));
            }
            return this;
        }

        public CoreNLPParseNodeBuilder withText(Optional<String> text) {
            this.text = text;
            return this;
        }

        public CoreNLPParseNodeBuilder withToken(Optional<CoreNLPToken> token) {
            this.token = token;
            return this;
        }

        public CoreNLPParseNode build() {
            // we impose no requirement that text or token is present; verbs, noun phrases, etc., can all get dropped and parsers may find where they belonged.
            if (text.isPresent() && token.isPresent()) {
                checkState(text.get().equals(token.get().content()),
                        "if we have both text and a data they must be the same, but instead we have " + text
                                .get() + " and " + token.get().content());
            }
            final Optional<CoreNLPParseNode> head;
            if (children.isEmpty()) {
                head = Optional.absent();
            } else {
                head = headFinder.findHead(tag,
                        ImmutableList.copyOf(children));
            }
            final CoreNLPParseNode ret = new CoreNLPParseNode(tag, children, head, token);
            for (CoreNLPParseNode child : children) {
                child.setParent(ret);
            }
            return ret;
        }
    }


    public static Predicate<CoreNLPParseNode> isTerminal() {
        return new Predicate<CoreNLPParseNode>() {
            @Override
            public boolean apply(final CoreNLPParseNode input) {
                return input.terminal();
            }
        };
    }

    public Iterable<CoreNLPParseNode> preorderDFSTraversal() {
        return new Iterable<CoreNLPParseNode>() {
            @Override
            public Iterator<CoreNLPParseNode> iterator() {
                return new PreorderTraversal();
            }
        };
    }

    private class PreorderTraversal implements Iterator<CoreNLPParseNode> {

        private final Stack<CoreNLPParseNode> nodes = new Stack<>();

        public PreorderTraversal() {
            nodes.push(CoreNLPParseNode.this);
        }

        @Override
        public boolean hasNext() {
            return nodes.size() > 0;
        }

        @Override
        public CoreNLPParseNode next() {
            final CoreNLPParseNode n = nodes.pop();
            // reverse the nodes so we get everything in order; this is a stack after all
            for (final CoreNLPParseNode m : n.children().reverse()) {
                nodes.push(m);
            }
            return n;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Parses are immutable once created");
        }
    }
}
