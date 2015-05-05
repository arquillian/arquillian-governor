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
package org.arquillian.extension.governor.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.arquillian.extension.governor.configuration.GovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public final class TestMethodExecutionRegister
{
    private static final List<MethodExecutionDecision> methodExecutionDecisions = new ArrayList<TestMethodExecutionRegister.MethodExecutionDecision>();

    private static GovernorConfiguration governorConfiguration;

    private static ExecutionDecision DEFAULT_EXECUTION_DECISION = ExecutionDecision.execute("default execution");

    public static void clear()
    {
        methodExecutionDecisions.clear();
    }

    public static void put(String testMethod, Class<? extends Annotation> annotation, ExecutionDecision executionDecision)
    {
        MethodExecutionDecision methodExecutionDecision = new MethodExecutionDecision(testMethod, annotation, executionDecision);

        methodExecutionDecisions.add(methodExecutionDecision);
    }

    public static List<MethodExecutionDecision> getAll()
    {
        return Collections.unmodifiableList(methodExecutionDecisions);
    }

    public static ExecutionDecision resolve(Method testMethod, Class<? extends Annotation> annotation)
    {
        String annotationName = annotation.getName();

        if (annotationName.equals(TestMethodExecutionRegister.governorConfiguration.getIgnoreOnly()))
        {
            return DEFAULT_EXECUTION_DECISION;
        }

        for (final MethodExecutionDecision methodExecutionDecision : methodExecutionDecisions)
        {
            if (methodExecutionDecision.getTestMethod().equals(testMethod.toString()) && methodExecutionDecision.getAnnotation() == annotation)
            {
                return methodExecutionDecision.getExecutionDecision();
            }
        }

        return DEFAULT_EXECUTION_DECISION;
    }

    public static final class MethodExecutionDecision
    {
        private final String testMethod;

        private final Class<? extends Annotation> annotation;

        private final ExecutionDecision executionDecision;

        public MethodExecutionDecision(String testMethod, Class<? extends Annotation> annotation, ExecutionDecision executionDecision) {
            Validate.notNull(testMethod, "Test method has to be specified.");
            Validate.notNull(annotation, "Annotation has to be specified.");
            Validate.notNull(executionDecision, "Execution decision has to be specified.");

            this.testMethod = testMethod;
            this.annotation = annotation;
            this.executionDecision = executionDecision;
        }

        public String getTestMethod()
        {
            return testMethod;
        }

        public Class<? extends Annotation> getAnnotation()
        {
            return annotation;
        }

        public ExecutionDecision getExecutionDecision()
        {
            return executionDecision;
        }
    }

    public static void setConfigration(GovernorConfiguration governorConfiguration)
    {
        Validate.notNull(governorConfiguration, "GovernorConfiguration must be specified.");
        TestMethodExecutionRegister.governorConfiguration = governorConfiguration;
    }
}
