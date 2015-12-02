package com.bbn.nlp.corpora.ere;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.serif.apf.APFArgument;
import com.bbn.serif.apf.APFDocument;
import com.bbn.serif.apf.APFEntity;
import com.bbn.serif.apf.APFEntityArgument;
import com.bbn.serif.apf.APFEntityMention;
import com.bbn.serif.apf.APFEvent;
import com.bbn.serif.apf.APFEventMention;
import com.bbn.serif.apf.APFRelation;
import com.bbn.serif.apf.APFRelationMention;
import com.bbn.serif.apf.APFSpan;
import com.bbn.serif.apf.APFTime;
import com.bbn.serif.apf.APFTimeArgument;
import com.bbn.serif.apf.APFTimeMention;
import com.bbn.serif.apf.APFValue;
import com.bbn.serif.apf.APFValueArgument;
import com.bbn.serif.apf.APFValueMention;
import com.bbn.serif.apf.APFEventsToTACSlotsMapper;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.*;

public final class EREtoAPF {
  private static final Logger log = LoggerFactory.getLogger(EREtoAPF.class);

  private final EREtoACETypeMapper ereToAceTypeMapper;
  // for ERE events to TAC slot conversion
  private final APFEventsToTACSlotsMapper slotsMapper;


  private EREtoAPF(final EREtoACETypeMapper ereToAceTypeMapper,
      APFEventsToTACSlotsMapper slotsMapper) {
    this.ereToAceTypeMapper = ereToAceTypeMapper;
    this.slotsMapper = checkNotNull(slotsMapper);
  }

  public static EREtoAPF from(final Parameters params) throws IOException {
    final EREtoACETypeMapper ereToAceTypeMapper = EREtoACETypeMapper.from(params);

    return new EREtoAPF(ereToAceTypeMapper, APFEventsToTACSlotsMapper.loadFrom(params));
  }

  public EREtoACETypeMapper getEREtoACETypeMapper() {
    return ereToAceTypeMapper;
  }

  public APFDocument toAPFDocument(final EREDocument ereDocument) {
    Map<String, Object> idMap = Maps.newHashMap();
    Map<String, String> mentionToCorefId = Maps.newHashMap();

    APFDocument.Builder builder = APFDocument.builder(ereDocument.getDocId());

    final String sourceType = ereDocument.getSourceType();
    final List<EREEntity> ereEntities = ereDocument.getEntities();
    final List<EREFiller> ereFillers = ereDocument.getFillers();
    final List<ERERelation> ereRelations = ereDocument.getRelations();
    final List<EREEvent> ereEvents = ereDocument.getEvents();

    Map<String, APFEntity> entities = Maps.newHashMap();
    // first, convert all the entity and entity mentions
    for(final EREEntity e : ereEntities) {
      final APFEntity apfEntity = toAPFEntity(e);

      idMap.put(apfEntity.getID(), apfEntity);
      for(final APFEntityMention m : apfEntity.getMentions()) {
        idMap.put(m.getID(), m);
        mentionToCorefId.put(m.getID(), apfEntity.getID());
      }

      entities.put(apfEntity.getID(), apfEntity);
      //builder.withEntity(apfEntity);
    }

    Map<String, APFEntityMention> titleMentions = Maps.newHashMap();
    // convert the fillers to entities, values, and times
    for(final EREFiller filler : ereFillers) {
      final Symbol apfType = APFMentionType(filler);

      if(apfType == ENTITY) {
        final APFEntity e = toAPFEntity(filler);
        final APFEntityMention m = e.getMentions().get(0);
        idMap.put(m.getID(), m);
        if("title".equals(filler.getType())) {
          titleMentions.put(m.getID(), m);     // we will place this into the appropriate APFEntity later
        }
        else {
          idMap.put(e.getID(), e);
          mentionToCorefId.put(m.getID(), e.getID());
          entities.put(e.getID(), e);
          //builder.withEntity(e);
        }
      }
      else if(apfType == VALUE) {
        final APFValue v = toAPFValue(filler);
        final APFValueMention m = v.getMentions().get(0);
        idMap.put(v.getID(), v);
        idMap.put(m.getID(), m);
        mentionToCorefId.put(m.getID(), v.getID());
        builder.withValue(v);
      }
      else if(apfType == TIME) {
        final APFTime t = toAPFTime(filler);
        final APFTimeMention m = t.getMentions().get(0);
        idMap.put(t.getID(), t);
        idMap.put(m.getID(), m);
        mentionToCorefId.put(m.getID(), t.getID());
        builder.withTime(t);
      }
    }

    // convert relations
    for(final ERERelation relation : ereRelations) {
      final APFRelation apfRelation = toAPFRelation(relation, idMap, mentionToCorefId);

      for(final APFRelationMention rm : apfRelation.getRelationMentions()) {
        for(final Map.Entry<String, APFArgument> entry : rm.getArguments().entrySet()) {
          final APFArgument relationArg = entry.getValue();
          if(titleMentions.containsKey(relationArg.getID())) {  // if this relation argument is a title filler
            APFArgument otherArg = null;
            if("Arg-1".equals(entry.getKey())) {
              otherArg = rm.getArguments().get("Arg-2");
            }
            else if("Arg-2".equals(entry.getKey())) {
              otherArg = rm.getArguments().get("Arg-1");
            }
            if(otherArg instanceof APFEntityArgument) {     // this must be true
              final APFEntityArgument entityArgument = (APFEntityArgument) otherArg;
              final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(entityArgument.getID()) );

              List<APFEntityMention> entityMentions = Lists.newArrayList();
              entityMentions.addAll(entity.getMentions());
              entityMentions.add(titleMentions.get(relationArg.getID()));
              final APFEntity newEntity = entity.copyWithMentions(ImmutableList.copyOf(entityMentions));

              idMap.put(newEntity.getID(), newEntity);
              mentionToCorefId.put(relationArg.getID(), newEntity.getID());
              entities.put(newEntity.getID(), newEntity);
            }


          }
        }
      }

      builder.withRelation(apfRelation);
    }

    // convert events
    for(final EREEvent event : ereEvents) {
      final APFEvent apfEvent = toAPFEvent(event, idMap, mentionToCorefId);
      builder.withEvent(apfEvent);
    }

    // convert ERE events into TAC slots
    for(final EREEvent event : ereEvents) {
      final List<APFRelation> apfRelations = toTACslot(event, idMap, mentionToCorefId);
      for(APFRelation apfRelation : apfRelations)
        builder.withRelation(apfRelation);
    }

    for(final APFEntity e : entities.values()) {
      builder.withEntity(e);
    }

    final APFDocument apfDocument = builder.build();
    return apfDocument;
  }


  // convert ERE events into TAC slots
  // TODO: coref relation mentions: 1) within-event, 2) cross-event, e.g., killed (monkey, GPE) in 2 events, probably by different people.
  public List<APFRelation> toTACslot (final EREEvent event,
      final Map<String, Object> idMap, final Map<String, String> mentionToCorefId) {
    List<APFRelation> ret = new ArrayList<APFRelation>();

    final APFEvent apfEvent = toAPFEvent(event, idMap, mentionToCorefId);
    for(APFEventMention apfEventMention : apfEvent.getEventMentions()) {

      Map<APFRelationMention, String> apfRelationMention2slot =
          slotsMapper.getSlotFromAPFEventMention(apfEventMention,
              apfEvent.getType(), apfEvent.getSubtype());

      for (APFRelationMention apfRelationMention : apfRelationMention2slot.keySet()) {
        APFRelation.Builder apfRelationBuilder =
            APFRelation.builder("R-" + apfRelationMention.getID(),
                "TAC", apfRelationMention2slot.get(apfRelationMention), "NA", "NA");
//        APFRelation.Builder apfRelationBuilder =
//            APFRelation.builder("R-" + apfRelationMention.getID(),
//                "", apfRelationMention2slot.get(apfRelationMention), "NA", "NA"); // be faithful to its name
        ret.add(apfRelationBuilder.build());
      }
    }
    return ret;
 }
  //

  // ============== Relation START =============
  private static ImmutableSet<APFArgument> getRelationArguments(final ImmutableList<APFRelationMention> relationMentions) {
    final ImmutableSet.Builder<APFArgument> ret = ImmutableSet.builder();

    for(final APFRelationMention rm : relationMentions) {
      final ImmutableMap<String, APFArgument> arguments = rm.getArguments();

      for(Map.Entry<String, APFArgument> entry : arguments.entrySet()) {
        final String role = entry.getKey();
        final APFArgument relationArg = entry.getValue();
        ret.add(relationArg);
      }
    }

    return ret.build();
  }

  private APFRelation toAPFRelation(final ERERelation relation,
      final Map<String, Object> idMap, final Map<String, String> mentionToCorefId) {

    final String realis = relation.getRelationMentions().get(0).getRealis();
    final String modality = "true".equals(realis) ? "Asserted" : "Other";

    final String id = relation.getID();

//  final String type = relation.getType();
//  final String subtype = relation.getSubtype();
    String type = relation.getType();
    String subtype = relation.getSubtype();
    Optional<String> tacSlot = ereToAceTypeMapper.mapERErelationSubTypeToTACslot(
        relation.getType() + "." + relation.getSubtype());
    if(tacSlot.isPresent()) {
      type = "TAC";
//      type = ""; // faithful to its name
      subtype = tacSlot.get();
    }

    APFRelation.Builder builder = APFRelation.builder(id, type, subtype, UNSPECIFIED.toString(), modality);

    final ImmutableList<APFRelationMention> relationMentions = toAPFRelationMentions(relation.getRelationMentions(), idMap);

    // go over all relation mentions to get all arguments of this relation
    final ImmutableSet<APFArgument> relationArguments = getRelationArguments(relationMentions);

    Map<String, APFArgument> relationArgsMap = Maps.newHashMap();
    for(final APFArgument arg : relationArguments) {
      relationArgsMap.put(arg.getRole(), arg);
    }

    // and then add them as arguments to the relation builder
    for(final APFArgument arg : relationArguments) {
      if(arg instanceof APFEntityArgument) {
        final APFEntityArgument entityArgument = (APFEntityArgument) arg;
        if(mentionToCorefId.containsKey(entityArgument.getID())) {
          final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(entityArgument.getID()) );
          builder.withArgument(arg.getRole(), entity);
        }
        else {
          // there can only be one possibility. This argument is 'role'
          assert("personalsocial".equals(type));
          assert("role".equals(subtype));

          if("Arg-1".equals(arg.getRole())) {
            final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(relationArgsMap.get("Arg-2").getID()) );
            builder.withArgument(arg.getRole(), entity);
          }
          else if("Arg-2".equals(arg.getRole())) {
            final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(relationArgsMap.get("Arg-1").getID()) );
            builder.withArgument(arg.getRole(), entity);
          }

          //System.out.println(type + " " + subtype);
          //System.exit(0);
        }
      }
      else if(arg instanceof APFValueArgument) {
        final APFValueArgument valueArgument = (APFValueArgument) arg;
        final APFValue value = (APFValue) idMap.get( mentionToCorefId.get(valueArgument.getID()) );
        builder.withArgument(arg.getRole(), value);
      }
      else if(arg instanceof APFTimeArgument) {
        final APFTimeArgument timeArgument = (APFTimeArgument) arg;
        final APFTime time = (APFTime) idMap.get( mentionToCorefId.get(timeArgument.getID()) );
        builder.withArgument(arg.getRole(), time);
      }
    }

    for(final APFRelationMention em : relationMentions) {
      builder.withRelationMention(em);
    }

    final APFRelation apfRelation = builder.build();
    return apfRelation;
  }

  private ImmutableList<APFRelationMention> toAPFRelationMentions(final ImmutableList<ERERelationMention> relationMentions,
      final Map<String, Object> idMap) {
    final ImmutableList.Builder<APFRelationMention> ret = ImmutableList.builder();

    for(final ERERelationMention rm : relationMentions) {
      ret.add(toAPFRelationMention(rm, idMap));
    }

    return ret.build();
  }

  private APFRelationMention toAPFRelationMention(final ERERelationMention ereRelationMention,
      final Map<String, Object> idMap) {

    APFRelationMention.Builder builder = APFRelationMention.builder(ereRelationMention.getID(), Optional.<APFSpan>absent(), "Other");

    final ImmutableMap<String, EREArgument> arguments = ereRelationMention.getArguments();

    for(final Map.Entry<String, EREArgument> entry : arguments.entrySet()) {
      final String role = "rel_arg1".equals(entry.getKey())? "Arg-1" : "Arg-2";
      final EREArgument ereArg = entry.getValue();

      if(ereArg instanceof EREEntityArgument) {
        final EREEntityArgument entityArg = (EREEntityArgument) ereArg;
        final APFEntityMention mention = (APFEntityMention) idMap.get(entityArg.getID());
        builder.withArgument(role, APFEntityArgument.from(role, mention));
      }
      else if(ereArg instanceof EREFillerArgument) {
        final EREFillerArgument fillerArg = (EREFillerArgument) ereArg;

        final Object obj = idMap.get(fillerArg.getID()+"-1");
        if(obj instanceof APFEntityMention) {
          final APFEntityMention apfEntityMention = (APFEntityMention) obj;
          builder.withArgument(role, APFEntityArgument.from(role, apfEntityMention));
        }
        else if(obj instanceof APFValueMention) {
          final APFValueMention apfValueMention = (APFValueMention) obj;
          builder.withArgument(role, APFValueArgument.from(role, apfValueMention));

        }
        else if(obj instanceof APFTimeMention) {
          final APFTimeMention apfTimeMention = (APFTimeMention) obj;
          builder.withArgument(role, APFTimeArgument.from(role, apfTimeMention));
        }
      }
    }

    final APFRelationMention relationMention = builder.build();
    return relationMention;
  }


  // ============== Relation END ============


  // ============== Event START =============
  private APFEvent toAPFEvent(final EREEvent event,
      final Map<String, Object> idMap, final Map<String, String> mentionToCorefId) {
    final ImmutableList<APFEventMention> eventMentions = toAPFEventMentions(event.getEventMentions(), idMap);

    final String ereEventType = event.getEventMentions().get(0).getType();
    final String ereEventSubtype = event.getEventMentions().get(0).getSubtype();
    final String apfEventType = ereToAceTypeMapper.mapEventType(ereEventType);
    final String apfEventSubtype = ereToAceTypeMapper.mapEventSubtype(ereEventSubtype);

    final String realis = event.getEventMentions().get(0).getRealis();
    String modality;
    String polarity;
    String genericity;
    String tense = UNSPECIFIED.toString();

    if("actual".equals(realis)) {
      modality = "Asserted";
      polarity = "Positive";
      genericity = "Specific";
    } else if("generic".equals(realis)) {
      modality = "Asserted";
      polarity = "Positive";
      genericity = "Generic";
    } else {
      modality = "Other";
      polarity = "Negative";
      genericity = "Specific";
    }

    APFEvent.Builder builder = APFEvent.builder(event.getID(), apfEventType, apfEventSubtype, modality, polarity, genericity, tense);

    // go over all event mentions to get all arguments of this event
    final ImmutableSet<APFArgument> eventArguments = getEventArguments(eventMentions);

    Map<String, APFArgument> eventArgsMap = Maps.newHashMap();
    for(final APFArgument arg : eventArguments) {
      eventArgsMap.put(arg.getRole(), arg);
    }

    // and then add them as arguments to the event builder
    for(final APFArgument arg : eventArguments) {
      if(arg instanceof APFEntityArgument) {
        final APFEntityArgument entityArgument = (APFEntityArgument) arg;

        if(mentionToCorefId.containsKey(entityArgument.getID())) {
          final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(entityArgument.getID()) );
          builder.withArgument(arg.getRole(), entity);
        }
        else {
          // there can only be one possibility. This argument is 'role'
          assert("Position".equals(arg.getRole()));

          if(eventArgsMap.containsKey("Person")) {
            final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(eventArgsMap.get("Person").getID()) );
            builder.withArgument(arg.getRole(), entity);
          }

          //System.out.println(type + " " + subtype);
          //System.exit(0);
        }

        //if(!mentionToCorefId.containsKey(entityArgument.getID())) {
        //  System.out.println("cannot find " + entityArgument.getID());
        //}
        //final APFEntity entity = (APFEntity) idMap.get( mentionToCorefId.get(entityArgument.getID()) );
        //builder.withArgument(arg.getRole(), entity);
      }
      else if(arg instanceof APFValueArgument) {
        final APFValueArgument valueArgument = (APFValueArgument) arg;
        final APFValue value = (APFValue) idMap.get( mentionToCorefId.get(valueArgument.getID()) );
        builder.withArgument(arg.getRole(), value);
      }
      else if(arg instanceof APFTimeArgument) {
        final APFTimeArgument timeArgument = (APFTimeArgument) arg;
        final APFTime time = (APFTime) idMap.get( mentionToCorefId.get(timeArgument.getID()) );
        builder.withArgument(arg.getRole(), time);
      }
    }

    for(final APFEventMention em : eventMentions) {
      builder.withEventMention(em);
    }

    final APFEvent apfEvent = builder.build();
    return apfEvent;
  }


  private ImmutableList<APFEventMention> toAPFEventMentions(final ImmutableList<EREEventMention> eventMentions,
      final Map<String, Object> idMap) {
    final ImmutableList.Builder<APFEventMention> ret = ImmutableList.builder();

    for(final EREEventMention em : eventMentions) {
      ret.add(toAPFEventMention(em, idMap));
    }

    return ret.build();
  }

  private APFEventMention toAPFEventMention(final EREEventMention eventMention, final Map<String, Object> idMap) {
    APFEventMention.Builder emBuilder = APFEventMention.builder(eventMention.getID(),
        Optional.<APFSpan>absent(), toAPFSpan(eventMention.getTrigger()));

    final String ereEventType = eventMention.getType();
    final String ereEventSubtype = eventMention.getSubtype();

    for(final EREArgument ereArg : eventMention.getArguments()) {
      if(ereArg instanceof EREEntityArgument) {
        final EREEntityArgument entityArg = (EREEntityArgument) ereArg;

        final String ereEventRole = entityArg.getRole();
        String apfEventRole;
        if("place".equals(ereEventRole)) {
          apfEventRole = PLACE.toString();
        }
        else {
          apfEventRole = ereToAceTypeMapper.mapEventRole(ereEventType, ereEventSubtype, ereEventRole);
        }

        final APFEntityMention mention = (APFEntityMention) idMap.get(entityArg.getID());
        final APFEntityArgument apfEntityArg = APFEntityArgument.from(apfEventRole, mention);

        emBuilder.withArgument(apfEntityArg);
      }
      else if(ereArg instanceof EREFillerArgument) {
        final EREFillerArgument fillerArg = (EREFillerArgument) ereArg;
        final String ereEventRole = fillerArg.getRole();

        String apfEventRole;
        if("place".equals(ereEventRole)) {
          apfEventRole = PLACE.toString();
        }
        else if("time".equals(ereEventRole)) {
          apfEventRole = TIME_WITHIN.toString();
        }
        else {
          apfEventRole = ereToAceTypeMapper.mapEventRole(ereEventType, ereEventSubtype, ereEventRole);
        }

        final Object obj = idMap.get(fillerArg.getID()+"-1");
        if(obj instanceof APFEntityMention) {
          final APFEntityMention apfEntityMention = (APFEntityMention) obj;
          emBuilder.withArgument(APFEntityArgument.from(apfEventRole, apfEntityMention));
        }
        else if(obj instanceof APFValueMention) {
          final APFValueMention apfValueMention = (APFValueMention) obj;
          emBuilder.withArgument(APFValueArgument.from(apfEventRole, apfValueMention));

        }
        else if(obj instanceof APFTimeMention) {
          final APFTimeMention apfTimeMention = (APFTimeMention) obj;
          emBuilder.withArgument(APFTimeArgument.from(apfEventRole, apfTimeMention));
        }
      }
    }

    final APFEventMention apfEventMention = emBuilder.build();
    return apfEventMention;
  }

  private static ImmutableSet<APFArgument> getEventArguments(final ImmutableList<APFEventMention> eventMentions) {
    final ImmutableSet.Builder<APFArgument> ret = ImmutableSet.builder();

    for(final APFEventMention em : eventMentions) {
      for(final APFArgument arg : em.getArguments()) {
        ret.add(arg);
      }
    }

    return ret.build();
  }
  // ============== Event END ==============

  // ============== Entity START =============
  private APFEntity toAPFEntity(final EREEntity e) {
    final String cls = "specific".equals(e.getSpecificity()) ? "SPC" : "GEN";

    final String ereEntityType = e.getType();
    final String apfEntityType = ereEntityType;     // no change

    APFEntity.Builder builder = APFEntity.builder(e.getID(), apfEntityType, NONE.toString(), cls);

    for(final EREEntityMention m : e.getMentions()) {
      builder.withMention(toAPFEntityMention(m));
    }

    final APFEntity entity = builder.build();
    return entity;
  }

  private static APFEntityMention toAPFEntityMention(final EREEntityMention m) {
    final APFSpan extent = toAPFSpan(m.getExtent());
    final APFSpan head = m.getHead().isPresent()? toAPFSpan(m.getHead().get()) : toAPFSpan(m.getExtent());

    final APFEntityMention entityMention = APFEntityMention.from(m.getID(), m.getType(), m.getType(), "FALSE", extent, head);
    return entityMention;
  }

  private static APFEntityArgument toAPFEntityArgument(final EREEntityArgument arg, final Map<String, Object> idMap) {
    final String role = arg.getRole();
    final APFEntityMention mention = (APFEntityMention) idMap.get(arg.getID());
    return APFEntityArgument.from(role, mention);
  }

  // ============== Entity END =============



  // ============== Fillers START =================
  /*
    age -> Value Numeric.Age*
    commodity Entity COM*
    crime -> Value Crime
    money -> Value Numeric.Money
    sentence -> Value Sentence
    time -> Timex2

    title ->
    We'll add this as a Entity PER and add its coref chain as well, based on richERE relation personalsocial.role
    Coref chain:
    - care has to be taken, e.g. entity {Richard Cannon, one of their lawyers} , filler {lawyers}
    - it is inaccurate to simply add "lawyers" as coref to Richard Cannon. So some checks have to be in place.
    We'll also try adding this as Value Job-Title and see if it goes through with Serif.

    url -> Value Contact-Info.URL
    vehicle -> Entity VEH
    weapon -> Entity WEA

    # asterisk '*' are not present in ACE.
  */
  private static Symbol APFMentionType(final EREFiller ereFiller) {
    final String type = ereFiller.getType();

    if( "age".equals(type) || "crime".equals(type) || "money".equals(type) ||
        "sentence".equals(type) || "url".equals(type)) {
      return VALUE;
    }
    else if("commodity".equals(type) || "title".equals(type) || "vehicle".equals(type) || "weapon".equals(type)) {
      return ENTITY;
    }
    else if("time".equals(type)) {
      return TIME;
    }
    else {
      return NONE;
    }
  }

  private APFEntity toAPFEntity(final EREFiller ereFiller) {
    final APFEntityMention entityMention = APFEntityMention.from(ereFiller.getID()+"-1",
        NOM.toString(), NOM.toString(), "FALSE", toAPFSpan(ereFiller.getExtent()), toAPFSpan(ereFiller.getExtent()));

    final String ereType = ereFiller.getType();
    final String apfType = ereToAceTypeMapper.mapFillerType(ereType);

    APFEntity.Builder builder = null;
    if("commodity".equals(ereType) || "title".equals(ereType) || "vehicle".equals(ereType) || "weapon".equals(ereType)) {
      builder = APFEntity.builder(ereFiller.getID(), apfType, NONE.toString(), "SPC");
    }

    if(builder!=null) {
      builder.withMention(entityMention);
      APFEntity entity = builder.build();
      return entity;
    }
    else {
      return null;
    }
  }

  private APFValue toAPFValue(final EREFiller ereFiller) {
    final APFValueMention valueMention = APFValueMention.from(ereFiller.getID()+"-1", toAPFSpan(ereFiller.getExtent()));

    final String ereType = ereFiller.getType();

    final String apfType = ereToAceTypeMapper.mapFillerType(ereType);
    final Optional<String> apfSubtype = ereToAceTypeMapper.mapFillerSubtype(ereType);
    final String apfSubtypeString = apfSubtype.isPresent()? apfSubtype.get() : NONE.toString();

    APFValue value = null;
    if( "age".equals(ereType) || "crime".equals(ereType) || "money".equals(ereType) ||
        "sentence".equals(ereType) || "url".equals(ereType)) {
      value = APFValue.from(ereFiller.getID(), apfType, apfSubtypeString, valueMention);
    }

    return value;
  }

  private APFTime toAPFTime(final EREFiller ereFiller) {
    final APFTimeMention timeMention = APFTimeMention.from(ereFiller.getID()+"-1", toAPFSpan(ereFiller.getExtent()));

    final String ereType = ereFiller.getType();
    final String apfType = ereToAceTypeMapper.mapFillerType(ereType);

    APFTime time = null;
    if("time".equals(ereType)) {
      time = APFTime.from(ereFiller.getID(), apfType, timeMention);
    }

    return time;
  }
  // ============== Fillers END ==============


  private static APFSpan toAPFSpan(final ERESpan span) {
    return APFSpan.from(span.getStart(), span.getEnd(), span.getText());
  }

  private static final Symbol ENTITY = Symbol.from("ENTITY");
  private static final Symbol VALUE = Symbol.from("VALUE");
  private static final Symbol TIME = Symbol.from("TIME");
  private static final Symbol NONE = Symbol.from("NONE");

  private static final Symbol NOM = Symbol.from("NOM");

  private static final Symbol PLACE = Symbol.from("Place");
  private static final Symbol TIME_WITHIN = Symbol.from("Time-Within");

  private static final Symbol OTHER = Symbol.from("Other");
  private static final Symbol UNSPECIFIED = Symbol.from("Unspecified");
}

