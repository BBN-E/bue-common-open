package com.bbn.bue.common.serialization.jackson.mixins;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.regex.Pattern;

/**
 * Fix that Java's regex Pattern is not Jackson-serializable
 */
public abstract class PatternMixin {

  @JsonCreator
  public static Pattern compile(@JsonProperty("regexString") String regexString,
      @JsonProperty("flags") int flags) {
    return null;
  }

  @JsonProperty("regexString")
  public abstract String pattern();

  @JsonProperty("flags")
  public abstract int flags();
}
