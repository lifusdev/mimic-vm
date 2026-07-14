package com.mimicvm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * marks a method for virtualization.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface VirtualizeMe {
}
