package com.bbn.bue.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD,
    ElementType.PACKAGE, ElementType.TYPE})
public @interface MoveToBUECommon {

}
