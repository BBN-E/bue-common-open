package com.bbn.nlp.corpora.lightERE;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Jay DeYoung
 */
public final class ERERelationMention {


  private final String id;
  private final List<EREArg> args;
  private final Optional<ERETrigger> trigger;

  private ERERelationMention(final String id, final List<EREArg> args,
      final Optional<ERETrigger> trigger) {
    this.id = id;
    this.args = args;
    this.trigger = trigger;
  }

  public String getId() {
    return id;
  }

  public List<EREArg> getArgs() {
    return args;
  }

  public Optional<ERETrigger> getTrigger() {
    return trigger;
  }

  @Override
  public String toString() {
    return "ERERelationMention{" +
        "id='" + id + '\'' +
        ", args=" + args +
        ", trigger=" + trigger +
        '}';
  }

  public static Builder builder(final String id) {
    return new Builder(id);
  }

  public static class Builder {

    private String id;
    private List<EREArg> args = Lists.newArrayList();
    private Optional<ERETrigger> trigger = Optional.absent();

    public Builder(final String id) {
      this.id = id;
    }

    public Builder setId(final String id) {
      this.id = id;
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

    public Builder setTrigger(final Optional<ERETrigger> trigger) {
      this.trigger = trigger;
      return this;
    }

    public ERERelationMention build() {
      return new ERERelationMention(this.id, this.args, this.trigger);
    }
  }
}

