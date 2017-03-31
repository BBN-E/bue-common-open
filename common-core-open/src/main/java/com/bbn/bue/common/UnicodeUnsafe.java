package com.bbn.bue.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates this code is not safe for input with Unicode characters outside the
 * basic multilingual plane.  For example, it may use {@link String#substring(int)},
 * which could split characters.
 *
 * While counting by codepoints is not necessarily meaningful (see e.g. http://utf8everywhere.org/),
 * using Unicode codepoint offsets is standard in LDC corpora, NIST evaluations, etc.,
 * so that is what most of our code uses.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
    ElementType.PACKAGE, ElementType.TYPE})
public @interface UnicodeUnsafe {
}
