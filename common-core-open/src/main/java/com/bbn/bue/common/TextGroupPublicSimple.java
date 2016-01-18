package com.bbn.bue.common;

import com.google.common.annotations.Beta;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this for simple classes which should have a from method,
 * but no builders or copy methods.
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
    // the template class can be prefixed with Abstract or with _
    // _ is preferred
    typeAbstract = {"_*"},
    // make the class annotated with this package private,
    // so API users only see the generated implementation, not
    // the template
    visibility = Value.Style.ImplementationVisibility.PUBLIC,
    // don't have builders or copy methods by default
    defaults = @Value.Immutable(builder = false, copy = false))
public @interface TextGroupPublicSimple {

}
