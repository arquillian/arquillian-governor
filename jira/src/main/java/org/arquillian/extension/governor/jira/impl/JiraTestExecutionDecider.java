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
package org.arquillian.extension.governor.jira.impl;

import org.arquillian.extension.governor.api.ClosePassedDecider;
import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestResult.Status;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class JiraTestExecutionDecider implements TestExecutionDecider, GovernorProvider {
    private static final Map<Method, Integer> lifecycleCountRegister = new HashMap<Method, Integer>();

    @Inject
    @ClassScoped
    private InstanceProducer<ExecutionDecision> executionDecision;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ClosePassedDecider> closePassedDecider;

    @Override
    public ExecutionDecision decide(Method testMethod) {
        return TestMethodExecutionRegister.resolve(testMethod, provides());
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Class<? extends Annotation> provides() {
        return Jira.class;
    }

    public void on(@Observes ExecutionDecisionEvent event, JiraGovernorClient jiraGovernorClient) {
        final ExecutionDecision executionDecision = this.executionDecision.get();

        if (executionDecision == null || executionDecision.getDecision() == Decision.DONT_EXECUTE) {
            return;
        }

        if (event.getAnnotation().annotationType() == provides()) {
            final Jira jiraIssue = (Jira) event.getAnnotation();

            this.executionDecision.set(jiraGovernorClient.resolve(jiraIssue));
        }
    }

    public void on(@Observes AfterTestLifecycleEvent event,
                   TestResult testResult,
                   GovernorRegistry governorRegistry,
                   JiraGovernorConfiguration jiraGovernorConfiguration) {
        int count = 0;
        try {
            final Integer c = lifecycleCountRegister.get(event.getTestMethod());
            count = (c != null ? c.intValue() : 0);
            if (count == 0) { //skip first event - see https://github.com/arquillian/arquillian-governor/pull/16#issuecomment-166590210
                return;
            }
            final ExecutionDecision decision = TestMethodExecutionRegister.resolve(event.getTestMethod(), provides());

            // if we passed some test method annotated with Jira, we may eventually close it

            if (jiraGovernorConfiguration.getClosePassed()) {
                // we decided we run this test method even it has annotation on it
                if (decision.getDecision() == Decision.EXECUTE
                        && (JiraGovernorStrategy.FORCING_EXECUTION_REASON_STRING).equals(decision.getReason())) {

                    for (final Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().entrySet()) {
                        if (entry.getKey().toString().equals(event.getTestMethod().toString())) {
                            for (final Annotation annotation : entry.getValue()) {
                                if (annotation.annotationType() == provides()) {
                                    closePassedDecider.get().setClosable(annotation, testResult.getStatus() == Status.PASSED);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            lifecycleCountRegister.put(event.getTestMethod(), ++count);
        }
    }

    public void on(@Observes AfterSuite event, JiraGovernorClient jiraGovernorClient) {
        for (final Map.Entry<Annotation, Boolean> entry : closePassedDecider.get().get().entrySet()) {
            final Annotation annotation = entry.getKey();
            if (annotation.annotationType() == provides() && entry.getValue()) {
                final String id = ((Jira) annotation).value();
                jiraGovernorClient.close(id);
            }
        }
    }
}
