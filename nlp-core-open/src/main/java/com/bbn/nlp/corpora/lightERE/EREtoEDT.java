package com.bbn.nlp.corpora.lightERE;

import com.bbn.bue.common.parameters.Parameters;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

/**
 * @author Jay DeYoung
 */
public class EREtoEDT {

  // it is the responsbility of each converter method to properly add and check this
  private final Set<String> deletedOrIgnoredIds = Sets.newHashSet();

  private EREtoEDT() {

  }

  public static EREtoEDT create(Parameters params) {
    return new EREtoEDT();
  }

  private static final Logger log = LoggerFactory.getLogger(EREtoEDT.class);

  public EREDocument convertDocumentToEDT(final EREDocument ereDoc, String source) {
    final ImmutableMap<Integer, Integer> offsetMap = lightEREOffsetToEDTOffset(source);
    return convertDocumentToEDT(ereDoc, offsetMap);
  }

  private EREDocument convertDocumentToEDT(final EREDocument ereDoc,
      final ImmutableMap<Integer, Integer> offsetMap) {
    final EREDocument.Builder result =
        EREDocument.builder(ereDoc.getDocid(), ereDoc.getSource_type());
    result.setConversation_id(ereDoc.getConversation_id());
    result.setKit_id(ereDoc.getKit_id());
    for (EREEntity ereEntity : convertEREEntities(ereDoc.getEntities(), offsetMap)) {
      result.withEntity(ereEntity);
    }
    for (EREEvent ereEvent : convertEREEvents(ereDoc.getEvents(), offsetMap)) {
      result.withEvent(ereEvent);
    }
    for (ERERelation ereRelation : convertERERelations(ereDoc.getRelations(), offsetMap)) {
      result.withRelation(ereRelation);
    }

    return result.build();
  }

  private Iterable<? extends EREEvent> convertEREEvents(final List<EREEvent> events,
      final ImmutableMap<Integer, Integer> offsetMap) {
    final ImmutableList.Builder<EREEvent> result = ImmutableList.builder();
    for (final EREEvent raw : events) {
      final EREEvent.Builder EDT = EREEvent.builder(raw.getId(), raw.getName());
      for (final EREEventMention rawM : raw.getEvents()) {
        final EREEventMention.Builder emEDT =
            EREEventMention.builder(rawM.getId(), rawM.getType(), rawM.getSubtype());
        //filter the arguments
        final List<EREArg> args = convertArgs(rawM.getArgs());
        if (args.size() > 0) {
          emEDT.withArgs(args);
        } else {
          // it is not an error for an event mention to lack arguments
        }
        emEDT.withPlaces(rawM.getPlaces());
        //process date, ok if we lose it since it's not required by the schema
        final Optional<EREDate> rawDate = rawM.getDate();
        // only check the start offset because we know that no extent overlaps the wakas (<>)
        if (!rawDate.isPresent() || !offsetMap
            .containsKey(rawDate.get().getDateExtent().getOffset())) {
          emEDT.setDate(Optional.<EREDate>absent());
        } else {
          final EREDate.Builder EDTConverted = EREDate.builder();
          final EREDateExtent rawDateExtent = rawDate.get().getDateExtent();
          EDTConverted.setNormalizedDate(rawDate.get().getNormalizedDate());

          EDTConverted.setDateExtent(EREDateExtent.fromPreciseOffsets(rawDateExtent.getSource(),
              offsetMap.get(rawDateExtent.getOffset()),
              offsetMap.get(rawDateExtent.getEndOffset()),
              rawDateExtent.getDateContent()));
          emEDT.setDate(Optional.of(EDTConverted.build()));
        }
        // no trigger, no argument
        final Optional<ERETrigger> convertedTrigger =
            triggerFromOffset(rawM.getTrigger(), offsetMap);
        if (convertedTrigger.isPresent()) {
          emEDT.setTrigger(convertedTrigger.get());
        } else {
          // since this event mention lacks a trigger, don't add it
          deletedOrIgnoredIds.add(rawM.getId());
          continue;
        }

        EDT.withEventMention(emEDT.build());
      }
      result.add(EDT.build());
    }
    return result.build();
  }

  private List<EREArg> convertArgs(final List<EREArg> args) {
    return ImmutableList.copyOf(Iterables.filter(args, new Predicate<EREArg>() {
      @Override
      public boolean apply(final EREArg input) {
        return !deletedOrIgnoredIds.contains(input.getEntityId()) && !deletedOrIgnoredIds
            .contains(input.getEntityMentionId());
      }
    }));
  }

  private Optional<ERETrigger> triggerFromOffset(final ERETrigger base,
      final ImmutableMap<Integer, Integer> offsetMap) {
    final int rawOffset = base.getOffset();
    final int rawOffsetEnd = base.getEndOffset();
    final String source = base.getSource();
    final String trigger = base.getTrigger();

    final Integer newOffset = offsetMap.get(rawOffset);
    if (newOffset == null) {
      return Optional.absent();
    }
    final int newStart = newOffset;
    final Integer newEnd = offsetMap.get(rawOffsetEnd);
    if (newEnd == null) {
      log.info("trigger base {} new start is {} but new end is {}", base, newStart, newEnd);
    }
    return Optional.of(ERETrigger.fromPreciseOffsets(source, trigger, newStart, newEnd));
  }

  private Iterable<? extends ERERelation> convertERERelations(
      final List<ERERelation> relations,
      final ImmutableMap<Integer, Integer> offsetMap) {
    final ImmutableList.Builder<ERERelation> result = ImmutableList.builder();
    for (final ERERelation raw : relations) {
      final ERERelation.Builder EDTRelation =
          ERERelation.builder(raw.getId(), raw.getType(), raw.getSubtype());
      boolean addedAny = false;
      for (final ERERelationMention rawM : raw.getMentions()) {
        final ERERelationMention.Builder EDTMention = ERERelationMention.builder(rawM.getId());
        final List<EREArg> convertedArgs = convertArgs(rawM.getArgs());
        // if one half of a relation is deleted, there's no relation
        if (convertedArgs.size() >= 2) {
          EDTMention.withArgs(convertedArgs);
        } else {
          deletedOrIgnoredIds.add(rawM.getId());
          continue;
        }
        // trigger does not need to be checked for presence here; not required by schema (can be inferred from args)
        final Optional<ERETrigger> rawTrigger = rawM.getTrigger();
        if (rawTrigger.isPresent()) {
          EDTMention.setTrigger(triggerFromOffset(rawTrigger.get(), offsetMap));
        }
        EDTRelation.withRelationMention(EDTMention.build());
        addedAny = true;
      }
      if (addedAny) {
        result.add(EDTRelation.build());
      }
    }
    return result.build();
  }

  private Iterable<EREEntity> convertEREEntities(final List<EREEntity> entities,
      final ImmutableMap<Integer, Integer> offsetMap) {
    final ImmutableList.Builder<EREEntity> result = ImmutableList.builder();
    for (final EREEntity raw : entities) {
      final EREEntity.TYPE type;
      if (raw.getType().equals(EREEntity.TYPE.TITLE)) {
        log.warn("Warning! Entity with TITLE type! Changing to PER and ignoring links! {}", raw);
        type = EREEntity.TYPE.PER;
        // ignored
        deletedOrIgnoredIds.add(raw.getId());
      } else {
        type = raw.getType();
      }
      final EREEntity.Builder EDTEntity = EREEntity.builder(raw.getId(), type);
      EDTEntity.setName(raw.getName());
      boolean addedAny = false;
      for (final EREEntityMention rawM : raw.getMentions()) {
        final Optional<EREEntityMention> convertedMention = convertEntityMention(rawM, offsetMap);
        if (convertedMention.isPresent()) {
          addedAny = true;
          EDTEntity.withMention(convertedMention.get());
        } else {
          deletedOrIgnoredIds.add(rawM.getId());
        }
      }
      if (addedAny) {
        result.add(EDTEntity.build());
      } else {
        deletedOrIgnoredIds.add(raw.getId());
      }

    }
    return result.build();
  }

  private Optional<EREEntityMention> convertEntityMention(final EREEntityMention rawM,
      final ImmutableMap<Integer, Integer> offsetMap) {
    final EREEntityMention.NOUNTYPES nountypes;
    if (rawM.getNounType().name().equals("NA")) {
      log.warn("Warning! Mention with NA type! Changing to NOM and ignoring relation! FIXME {}",
          rawM);
      // ignored
      deletedOrIgnoredIds.add(rawM.getId());
      nountypes = EREEntityMention.NOUNTYPES.NOM;
//      return Optional.absent();
    } else {
      nountypes = rawM.getNounType();
    }
    final Integer newStart = offsetMap.get(rawM.getOffset());
    final Integer newEnd = offsetMap.get(rawM.getEndOffset());
    if (newStart == null) {
      deletedOrIgnoredIds.add(rawM.getId());
      return Optional.absent();
    }
    if (newEnd == null) {
      log.error("start is {} but end is {} for {}", newStart, newEnd, rawM);
      throw new RuntimeException(
          "invalid end conversion for " + rawM + " start went to " + newStart);
    }
    return Optional.of(EREEntityMention
        .fromPreciseOffsets(rawM.getId(), nountypes, rawM.getSource(), newStart, newEnd,
            rawM.getText()));
  }

  /**
   * lightERE offsets are indexed into the document, including text inside tags EDT offsets are
   *
   * TODO use the mapping in bue-common-open
   */
  private ImmutableMap<Integer, Integer> lightEREOffsetToEDTOffset(String document) {
    final ImmutableMap.Builder<Integer, Integer> offsetMap = ImmutableMap.builder();
    int EDT = 0;
    // lightERE treats these as one, not two (as an XML parser would)
    document = document.replaceAll("\\r\\n", "\n");
    for (int i = 0; i < document.length(); i++) {
      final String c = document.substring(i, i + 1);
      // skip <tags>
      if (c.equals("<")) {
        i = document.indexOf('>', i);
        continue;
      }
      offsetMap.put(i, EDT);
      EDT++;
    }
    return offsetMap.build();
  }
}


