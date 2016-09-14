package com.bbn.bue.common;

import com.google.common.annotations.Beta;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is the text group's default style for immutable objects generated using
 * the "Immutables.org" library.
 *
 * When making an immutable object X you want to do:
 * {@code
 * @TextGroupImmutable
 * @Immutable.Value
 * interface X extends WithX {
 *     // attributes here
 *
 *     class Builder extends ImmutableX.Builder {}
 * }
 */
@Beta
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Value.Style(
    // make the class annotated with this package private,
    // so API users only see the generated implementation, not
    // the template
    visibility = Value.Style.ImplementationVisibility.PACKAGE,
    overshadowImplementation = true)
public @interface TextGroupImmutable {
}
