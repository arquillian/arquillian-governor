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
package org.arquillian.extension.governor.github.impl;

import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.github.api.GitHub;
import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfiguration;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
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
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GitHubTestExecutionDecider implements TestExecutionDecider, GovernorProvider
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
        return GitHub.class;
    }

    public void on(@Observes ExecutionDecisionEvent event, GitHubGovernorClient gitHubGovernorClient)
    {
        ExecutionDecision executionDecision = this.executionDecision.get();

        if (executionDecision == null || executionDecision.getDecision() == Decision.DONT_EXECUTE)
        {
            return;
        }

        if (event.getAnnotation().annotationType() == provides())
        {
            GitHub gitHubIssue = (GitHub) event.getAnnotation();

            this.executionDecision.set(gitHubGovernorClient.resolve(gitHubIssue));
        }
    }

    public void on(@Observes After event,
        TestResult testResult,
        GovernorRegistry governorRegistry,
        GitHubGovernorConfiguration gitHubGovernorConfiguration,
        GitHubGovernorClient gitHubGovernorClient)
    {
        final ExecutionDecision decision = TestMethodExecutionRegister.resolve(event.getTestMethod(), provides());

        // if we passed some test method annotated with GitHub, we may eventually close it

        if (gitHubGovernorConfiguration.getClosePassed())
        {
            // we decided we run this test method even it has annotation on it
            if (testResult.getStatus() == Status.PASSED
                && decision.getDecision() == Decision.EXECUTE
                && (decision.getReason().equals(GitHubGovernorStrategy.FORCING_EXECUTION_REASON_STRING)))
            {

                for (Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().entrySet())
                {
                    if (entry.getKey().toString().equals(event.getTestMethod().toString()))
                    {
                        for (Annotation annotation : entry.getValue())
                        {
                            if (annotation.annotationType() == provides())
                            {
                                String id = ((GitHub) annotation).value();
                                gitHubGovernorClient.close(id);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}

