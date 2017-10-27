package com.bbn.nlp.languages;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
    ElementType.PACKAGE, ElementType.TYPE})
public @interface LanguageSpecific {

  String[] value();
}
