package com.sean.springmvc.annotation;


import java.lang.annotation.*;

/**
 * 元注解
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {

    String value() default "";

}
