/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.extension.governor.redmine.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.arquillian.extension.governor.api.Governor;

/**
 * Place this annotation on a test method with Redmine issue (e.g. {@literal @Redmine(1)})
 *
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 *
 */
@Governor
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
public @interface Redmine
{
    /**
     *
     * @return issue ID number
     */
    String value() default "";

    boolean force() default false;

    /**
     *
     * @return if TRUE the issue will be reopened if it is closed and test fails.
     */
    boolean openFailed() default false;
}
