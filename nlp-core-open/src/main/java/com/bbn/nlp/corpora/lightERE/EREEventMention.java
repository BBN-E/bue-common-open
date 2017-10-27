package com.bbn.nlp.corpora.lightERE;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jay DeYoung
 */
public final class EREEventMention {

  private final String id;
  private final TYPE type;
  private final SUBTYPE subtype;

  private final ERETrigger trigger;
  private final List<EREArg> args;
  private final List<EREPlace> places;
  private final Optional<EREDate> date;

  private EREEventMention(final String id, final TYPE type, final SUBTYPE subtype,
      final ERETrigger trigger, final List<EREArg> args, final List<EREPlace> places,
      final Optional<EREDate> date) {
    this.id = id;
    this.type = type;
    this.subtype = subtype;
    this.trigger = trigger;
    this.args = args;
    this.places = places;
    this.date = date;
  }

  public String getId() {
    return id;
  }

  public TYPE getType() {
    return type;
  }

  public SUBTYPE getSubtype() {
    return subtype;
  }

  public ERETrigger getTrigger() {
    return trigger;
  }

  public List<EREArg> getArgs() {
    return args;
  }

  public List<EREPlace> getPlaces() {
    return places;
  }

  public Optional<EREDate> getDate() {
    return date;
  }

  public enum TYPE {
    business,
    conflict,
    life,
    movement,
    personnel,
    transaction,
    justice,
    contact
  }

  public enum SUBTYPE {
    startorg,
    endorg,
    declarebankruptcy,
    mergeorg,
    attack,
    demonstrate,
    meet,
    communicate,
    beborn,
    marry,
    divorce,
    injure,
    die,
    transportperson,
    transportartifact,
    arrestjail,
    tryholdhearing,
    sentence,
    fine,
    chargeindict,
    sue,
    extradite,
    acquit,
    convict,
    releaseparole,
    appeal,
    execute,
    pardon,
    startposition,
    endposition,
    nominate,
    elect,
    transferownership,
    transfermoney,
  }

  @Override
  public String toString() {
    return "EREEventMention{" +
        "id='" + id + '\'' +
        ", type=" + type +
        ", subtype=" + subtype +
        ", trigger=" + trigger +
        ", \nargs=" + args +
        ", \nplaces=" + places +
        ", \ndate=" + date +
        '}';
  }

  public static Builder builder(final String id, final TYPE type, final SUBTYPE subtype) {
    return new Builder(id, type, subtype);
  }

  public static Builder builder(final String id, final String type, final String subtype) {
    return new Builder(id, TYPE.valueOf(type), SUBTYPE.valueOf(subtype));
  }

  public static class Builder {

    private String id;
    private TYPE type;
    private SUBTYPE subtype;
    private ERETrigger trigger;
    private List<EREArg> args = Lists.newArrayList();
    private List<EREPlace> places = Lists.newArrayList();
    private Optional<EREDate> date = Optional.absent();

    public Builder(final String id, final TYPE type,
        final SUBTYPE subtype) {
      this.id = id;
      this.type = type;
      this.subtype = subtype;
    }

    public Builder setId(final String id) {
      this.id = id;
      return this;
    }

    public Builder setType(final TYPE type) {
      this.type = type;
      return this;
    }

    public Builder setSubtype(final SUBTYPE subtype) {
      this.subtype = subtype;
      return this;
    }

    public Builder setTrigger(final ERETrigger trigger) {
      this.trigger = trigger;
      return this;
    }

    public Builder withArg(final EREArg arg) {
      this.args.add(arg);
      return this;
    }

    public Builder withArgs(final List<EREArg> args) {
      this.args.addAll(args);
      return this;
    }

    public Builder withPlace(final EREPlace place) {
      this.places.add(place);
      return this;
    }

    public Builder withPlaces(final List<EREPlace> places) {
      this.places.addAll(places);
      return this;
    }

    public Builder setDate(final Optional<EREDate> date) {
      this.date = date;
      return this;
    }

    public EREEventMention build() {
      return new EREEventMention(this.id, this.type, this.subtype, this.trigger, this.args,
          this.places, this.date);
    }
  }

  // guava style functions
  public static Function<EREEventMention, TYPE> typeFunction() {
    return new Function<EREEventMention, TYPE>() {
      @Override
      public TYPE apply(final EREEventMention input) {
        return input.getType();
      }
    };
  }

  public static Function<EREEventMention, SUBTYPE> subtypeFunction() {
    return new Function<EREEventMention, SUBTYPE>() {
      @Override
      public SUBTYPE apply(final EREEventMention input) {
        return input.getSubtype();
      }
    };
  }
}

