package com.bbn.bue.common.serialization.jackson;


import com.bbn.bue.common.serialization.jackson.mixins.PatternMixin;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.regex.Pattern;

import javax.inject.Inject;

public final class BUECommonOpenModule extends SimpleModule {

  private static final long serialVersionUID = 1L;

  @Inject
  public BUECommonOpenModule() {
    super("BUECommonOpenModule", new Version(2, 1, 0, null, "com.bbn.bue", "common-core-open"));
  }

  @Override
  public void setupModule(final SetupContext context) {
    context.setMixInAnnotations(Pattern.class, PatternMixin.class);
  }
}
