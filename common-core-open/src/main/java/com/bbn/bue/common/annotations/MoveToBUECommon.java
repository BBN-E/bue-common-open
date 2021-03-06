package com.bbn.bue.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark code in other repositories which should eventually be migrated to this one.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD,
    ElementType.PACKAGE, ElementType.TYPE})
public @interface MoveToBUECommon {

}
