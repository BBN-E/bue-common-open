package com.bbn.bue.common;

import com.google.common.annotations.Beta;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This used to be the text group's default style for immutable objects generated using
 * the "Immutables.org" library. Nowadays prefer {@link TextGroupImmutable}.
 * @deprecated Prefer {@link TextGroupImmutable}
 */
@Beta
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Value.Style(
    // use the stripped abstract type name as the immutable type name
    // rather than prefixing "Immutable" as the default would do.
    // This is so that if we have _FlexibleEventMention,
    // the generated implementation is FlexibleEventMention, not
    // ImmutableFlexibleEventMention
    typeImmutable = "*",
    // the template class must be prefixed with _
    typeAbstract = {"_*"},
    // make the class annotated with this package private,
    // so API users only see the generated implementation, not
    // the template
    visibility = Value.Style.ImplementationVisibility.PACKAGE)
@Deprecated
public @interface TextGroupPackageImmutable {

}
