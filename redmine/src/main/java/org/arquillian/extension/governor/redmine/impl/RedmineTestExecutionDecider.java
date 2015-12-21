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
package org.arquillian.extension.governor.redmine.impl;

import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
import org.arquillian.extension.governor.redmine.api.Redmine;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 *
 */
public class RedmineTestExecutionDecider implements TestExecutionDecider, GovernorProvider
{
    @Inject
    @ClassScoped
    private InstanceProducer<ExecutionDecision> executionDecision;

    @Override
    public ExecutionDecision decide(Method testMethod)
    {
        return TestMethodExecutionRegister.resolve(testMethod, provides());
    }

    @Override
    public int precedence()
    {
        return 0;
    }

    @Override
    public Class<? extends Annotation> provides()
    {
        return Redmine.class;
    }

    public void on(@Observes ExecutionDecisionEvent event, RedmineGovernorClient redmineGovernorClient)
    {
        ExecutionDecision executionDecision = this.executionDecision.get();

        if (executionDecision == null || executionDecision.getDecision() == Decision.DONT_EXECUTE)
        {
            return;
        }

        if (event.getAnnotation().annotationType() == provides())
        {
            Redmine redmineIssue = (Redmine) event.getAnnotation();

            this.executionDecision.set(redmineGovernorClient.resolve(redmineIssue));
        }
    }

    public void on(@Observes After event,
        TestResult testResult,
        GovernorRegistry governorRegistry,
        RedmineGovernorConfiguration redmineGovernorConfiguration,
        RedmineGovernorClient redmineGovernorClient)
    {
        final ExecutionDecision decision = TestMethodExecutionRegister.resolve(event.getTestMethod(), provides());

        // if we passed some test method annotated with GitHub, we may eventually close it

        if (redmineGovernorConfiguration.getClosePassed())
        {
            // we decided we run this test method even it has annotation on it
            if (testResult.getStatus() == Status.PASSED
                && decision.getDecision() == Decision.EXECUTE
                && (RedmineGovernorStrategy.FORCING_EXECUTION_REASON_STRING).equals(decision.getReason()))
            {

                for (Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().entrySet())
                {
                    if (entry.getKey().toString().equals(event.getTestMethod().toString()))
                    {
                        for (Annotation annotation : entry.getValue())
                        {
                            if (annotation.annotationType() == provides())
                            {
                                String id = ((Redmine) annotation).value();
                                redmineGovernorClient.close(id);
                                return;
                            }
                        }
                    }
                }
            }
        }
        if(redmineGovernorConfiguration.getOpenFailed())
        {
            if (testResult.getStatus() == Status.FAILED
                    && decision.getDecision() == Decision.EXECUTE
                    && (decision.getReason().equals(RedmineGovernorStrategy.FORCING_EXECUTION_REASON_STRING)))
            {

                for (Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().entrySet())
                {
                    if (entry.getKey().toString().equals(event.getTestMethod().toString()))
                    {
                        for (Annotation annotation : entry.getValue())
                        {
                            if (annotation.annotationType() == provides())
                            {
                                String id = ((Redmine) annotation).value();
                                redmineGovernorClient.open(id,testResult.getThrowable());
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}

