package com.bbn.bue.common.serialization.jackson;


import com.bbn.bue.common.evaluation.FMeasureCounts;
import com.bbn.bue.common.serialization.jackson.mixins.FMeasureCountsMixin;
import com.bbn.bue.common.serialization.jackson.mixins.SymbolMixin;
import com.bbn.bue.common.symbols.Symbol;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class BUECommonOpenModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  public BUECommonOpenModule() {
    super("BUECommonOpenModule", new Version(2, 1, 0, null, "com.bbn.bue", "common-core-open"));
  }

  @Override
  public void setupModule(final SetupContext context) {
    context.setMixInAnnotations(Symbol.class, SymbolMixin.class);
    context.setMixInAnnotations(FMeasureCounts.class, FMeasureCountsMixin.class);
  }
}
