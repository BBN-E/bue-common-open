package com.bbn.bue.common.strings.formatting;

import com.bbn.bue.common.StringUtils;
import com.bbn.bue.common.strings.offsets.AnnotatedOffsetRange;
import com.bbn.bue.common.strings.offsets.CharOffset;
import com.bbn.bue.common.strings.offsets.OffsetRange;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class XMLStyleAnnotationFormatter {

  private XMLStyleAnnotationFormatter() {
  }

  public static XMLStyleAnnotationFormatter create() {
    return new XMLStyleAnnotationFormatter();
  }

  public String format(String s, final Iterable<AnnotatedOffsetRange<CharOffset>> annotations) {
    final Optional<OffsetRange<CharOffset>> wholeStringBounds =
        OffsetRange.charOffsetsOfWholeString(s);
    if (wholeStringBounds.isPresent()) {
      return format(s, wholeStringBounds.get(), annotations);
    } else {
      return "";
    }
  }

  public String format(String s, final OffsetRange<CharOffset> snippetBounds,
      final Iterable<AnnotatedOffsetRange<CharOffset>> annotations) {
    final ImmutableList<AnnotatedOffsetRange<CharOffset>> clippedToSnippet =
        clipToSnippet(snippetBounds, annotations);
    final String snippet = StringUtils.substring(s, snippetBounds);
    final int snippetStartChar = snippetBounds.startInclusive().asInt();

    final StringBuilder result = new StringBuilder();
    int lastUncopiedIdx = 0;

    for (TagCursor tagCursor : new TagCursor(clippedToSnippet)) {
      final int tagOffset = tagCursor.offset() - snippetStartChar;

      if (tagCursor.tagType == TagCursor.Type.Start && lastUncopiedIdx < tagOffset) {
        result.append(snippet.substring(lastUncopiedIdx, tagOffset));
        lastUncopiedIdx = tagOffset;
      } else if (tagCursor.tagType == TagCursor.Type.End && lastUncopiedIdx <= tagOffset) {
        result.append(snippet.substring(lastUncopiedIdx, tagOffset + 1));
        lastUncopiedIdx = tagOffset + 1;
      }

      result.append(tagCursor.tag());
    }

    // get any trailing text after the last tag
    if (lastUncopiedIdx != snippet.length()) {
      result.append(snippet.substring(lastUncopiedIdx, snippet.length()));
    }

    return result.toString();
  }

  private ImmutableList<AnnotatedOffsetRange<CharOffset>> clipToSnippet(
      OffsetRange<CharOffset> snippet,
      Iterable<AnnotatedOffsetRange<CharOffset>> annotations) {
    final ImmutableList.Builder<AnnotatedOffsetRange<CharOffset>> ret = ImmutableList.builder();

    for (final AnnotatedOffsetRange<CharOffset> annotation : annotations) {
      final Optional<OffsetRange<CharOffset>> clippedBounds =
          annotation.range().clipToBounds(snippet);
      if (clippedBounds.isPresent()) {
        if (clippedBounds.get().equals(annotation.range())) {
          ret.add(annotation);
        } else {
          ret.add(AnnotatedOffsetRange.create(annotation.type(),
              clippedBounds.get(), annotation.attributes()));
        }
      } else {
        // if an annotation doesn't intersect the snippet, we don't care about it
        // because it won't be visible
      }
    }

    return ret.build();
  }

  private static class TagCursor implements Iterable<TagCursor> {

    private AnnotatedOffsetRange<CharOffset> offsetSpan;
    private Type tagType;

    public enum Type {Start, End}

    public TagCursor(Iterable<AnnotatedOffsetRange<CharOffset>> annotations) {
      tagsInStartingOrder = startTagOrder.sortedCopy(annotations);
      tagsInEndingOrder = endTagOrder.sortedCopy(annotations);
      numTags = tagsInStartingOrder.size();
      advanceNextStartingTag();
      advanceNextEndingTag();
    }

    public int offset() {
      if (tagType == Type.Start) {
        return offsetSpan.range().startInclusive().asInt();
      } else {
        return offsetSpan.range().endInclusive().asInt();
      }
    }

    public String tag() {
      if (tagType == Type.Start) {
        return XMLStyleTextAnnotations.toStartTag(offsetSpan);
      } else {
        return XMLStyleTextAnnotations.toEndTag(offsetSpan);
      }
    }

    private final List<AnnotatedOffsetRange<CharOffset>> tagsInStartingOrder;
    private final List<AnnotatedOffsetRange<CharOffset>> tagsInEndingOrder;
    private final int numTags;
    private int nextTagToStartIdx = 0;
    private int nextTagToEndIdx = 0;
    private AnnotatedOffsetRange<CharOffset> nextTagToStart;
    private AnnotatedOffsetRange<CharOffset> nextTagToEnd;

    private void advanceNextStartingTag() {
      nextTagToStart =
          (nextTagToStartIdx < numTags) ? tagsInStartingOrder.get(nextTagToStartIdx++) : null;
    }

    private void advanceNextEndingTag() {
      nextTagToEnd = (nextTagToEndIdx < numTags) ? tagsInEndingOrder.get(nextTagToEndIdx++) : null;
    }

    public Iterator<TagCursor> iterator() {
      return new Iterator<TagCursor>() {

        @Override
        public boolean hasNext() {
          return nextTagToStart != null || nextTagToEnd != null;
        }

        @Override
        public TagCursor next() {
          if (nextTagToStart != null && nextTagToEnd != null) {
            if (nextTagToStart.range().startInclusive().asInt() < nextTagToEnd.range()
                .endInclusive().asInt()
                // the or handles the case of a tag covering a single character
                || (nextTagToStart == nextTagToEnd)) {
              // we prefer to end things before we start them, at the same positions
              nextTagIsStart();
            } else {
              nextTagIsEnd();
            }
          } else if (nextTagToStart != null) {
            nextTagIsStart();
          } else if (nextTagToEnd != null) {
            nextTagIsEnd();
          } else {
            throw new NoSuchElementException();
          }

          return TagCursor.this;
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException();
        }
      };
    }

    private void nextTagIsStart() {
      offsetSpan = nextTagToStart;
      tagType = Type.Start;
      advanceNextStartingTag();
    }

    private void nextTagIsEnd() {
      offsetSpan = nextTagToEnd;
      tagType = Type.End;
      advanceNextEndingTag();
    }

    // for start tags, we want to break ties by longest length so that tags which
    // end later start first. This preserve proper nesting.
    private static final Ordering<AnnotatedOffsetRange<CharOffset>> startTagOrder =
        OffsetRange.<CharOffset>byStartOrdering()
            .compound(OffsetRange.<CharOffset>byLengthOrdering().reverse())
            .onResultOf(AnnotatedOffsetRange.<CharOffset>toOffsetRangeFunction());

    // for start tags, we want to break ties by shortest length so that tags which
    // start later end first. This preserve proper nesting.
    private static final Ordering<AnnotatedOffsetRange<CharOffset>> endTagOrder =
        OffsetRange.<CharOffset>byEndOrdering()
            .compound(OffsetRange.<CharOffset>byLengthOrdering())
            .onResultOf(AnnotatedOffsetRange.<CharOffset>toOffsetRangeFunction());
  }
}
