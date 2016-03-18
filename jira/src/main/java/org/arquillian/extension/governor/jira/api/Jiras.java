package org.arquillian.extension.governor.jira.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.arquillian.extension.governor.api.Governor;

@Governor
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface Jiras
{
    Jira[] value() default {};
    
    boolean force() default false;
}
