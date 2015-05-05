/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.governor.skipper.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.arquillian.extension.governor.api.Governor;

/**
 * Information from this annotation will be passed to resulting report.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@Governor
@Retention(RetentionPolicy.RUNTIME)
@Target( ElementType.METHOD )
@Documented
public @interface TestSpec {

    /**
     *
     * @return feature to which this test belongs to
     */
    String feature() default "";

    /**
     *
     * @return brief description of test
     */
    String test() default "";

    /**
     *
     * @return what is required to do or set up before executing this test
     */
    String prerequisites() default "";

    /**
     *
     * @return detailed steps in order to consider this test to be successful
     */
    String steps() default "";

    /**
     *
     * @return what is the expected result of the test
     */
    String assertion() default "";

    /**
     *
     * @return link to issue on an issue tracker
     */
    String issue() default "";

    /**
     * If this is set to {@link Status#MANUAL}, the annotated test method will be skipped from the execution.
     *
     * @return status of the automation of this test case
     */
    Status status() default Status.MANUAL;

    /**
     *
     * @return who is responsible for this, name, preferably e-mail address
     */
    String author() default "";
}
