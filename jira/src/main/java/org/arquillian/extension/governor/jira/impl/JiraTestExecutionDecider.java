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
package org.arquillian.extension.governor.jira.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.arquillian.extension.governor.api.GovernorClientRegistryRegistry;
import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JiraTestExecutionDecider implements TestExecutionDecider, GovernorProvider
{
    private static final Map<Method, Integer> lifecycleCountRegister = new HashMap<Method, Integer>();

    @Inject
    @ClassScoped
    private InstanceProducer<ExecutionDecision> executionDecision;

    @Inject
    @TestScoped
    private InstanceProducer<Jira> jiraAnnotationProducer;

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
        return Jira.class;
    }

    public void on(@Observes ExecutionDecisionEvent event)
    {
        ExecutionDecision executionDecision = this.executionDecision.get();

        if (executionDecision == null || executionDecision.getDecision() == Decision.DONT_EXECUTE)
        {
            return;
        }

        if (event.getAnnotation().annotationType() != provides())
        {
            return;
        }

        Jira jiraIssue = (Jira) event.getAnnotation();

        JiraGovernorClient governorClient = (JiraGovernorClient) GovernorClientRegistryRegistry
            .instance()
            .get(provides())
            .get(jiraIssue.server());

        executionDecision = governorClient.resolve(jiraIssue);

        if (executionDecision.getDecision() == Decision.EXECUTE)
        {
            JiraAnnotationRegister.add(jiraIssue);
        }

        this.executionDecision.set(executionDecision);
    }

    public void on(@Observes Before event, GovernorRegistry governorRegistry)
    {
        if (TestMethodExecutionRegister.resolve(event.getTestMethod(), provides()).getDecision() == Decision.EXECUTE)
        {
            for (Annotation annotation : governorRegistry.getAnnotationsForMethod(event.getTestMethod()))
            {
                if (annotation.annotationType() == provides() && JiraAnnotationRegister.contains(annotation))
                {
                    jiraAnnotationProducer.set((Jira) annotation);
                }
            }
        }
    }

    public void on(@Observes AfterTestLifecycleEvent event, TestResult testResult, Jira jiraIssue)
    {
        int count = 0;
        try
        {
            Integer c = lifecycleCountRegister.get(event.getTestMethod());
            count = (c != null ? c.intValue() : 0);
            if (count == 0)
            {// skip first event - see https://github.com/arquillian/arquillian-governor/pull/16#issuecomment-166590210
                return;
            }

            if (jiraIssue == null)
            {
                return;
            }

            JiraGovernorClient governorClient = (JiraGovernorClient) GovernorClientRegistryRegistry
                .instance()
                .get(Jira.class)
                .get(jiraIssue.server());

            // if we passed some test method annotated with Jira, we may eventually close it

            if (governorClient.getConfiguration().getClosePassed())
            {
                final ExecutionDecision decision = TestMethodExecutionRegister.resolve(event.getTestMethod(), provides());

                // we decided we run this test method even it has annotation on it
                if (testResult.getStatus() == Status.PASSED
                    && decision.getDecision() == Decision.EXECUTE
                    && (JiraGovernorStrategy.FORCING_EXECUTION_REASON_STRING).equals(decision.getReason()))
                {

                    governorClient.close(jiraIssue.value());
                }
            }
        } finally
        {
            lifecycleCountRegister.put(event.getTestMethod(), ++count);
        }
    }
}
