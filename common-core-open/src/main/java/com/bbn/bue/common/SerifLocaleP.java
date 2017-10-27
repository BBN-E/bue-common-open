package com.bbn.bue.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;


/**
 * The locale that Serif is running in.  This should correspond to the language being processed. If
 * Serif is processing multiple languages in one run, do not use this and prefer to specify the
 * locale for each language explicitly.  This will typically be bound by installing {@code
 * SerifLocaleM} (which is done automatically by the standard {@code SerifEnvironment} module).
 *
 * This should be used to annotate things of type {@link com.bbn.bue.common.SerifLocale}
 */
@Qualifier
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerifLocaleP {

}
