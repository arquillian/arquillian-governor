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

import org.arquillian.extension.governor.api.GovernorStrategy;
import org.eclipse.egit.github.core.Issue;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

import org.arquillian.extension.governor.github.api.Github;
import org.arquillian.extension.governor.github.configuration.GithubGovernorConfiguration;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GithubGovernorStrategy implements GovernorStrategy
{
    public static final String FORCING_EXECUTION_REASON_STRING = "forcing execution";
    public static final String SKIPPING_EXECUTION_REASON_STRING = "Skipping %s. Status %s.";
    public static final Object GITHUB_CLOSED_STRING = "closed";

    private GithubGovernorConfiguration githubGovernorConfiguration;
    private Github annotation;
    private Issue githubIssue;

    public GithubGovernorStrategy(GithubGovernorConfiguration githubGovernorConfiguration) 
    {
        Validate.notNull(githubGovernorConfiguration, "Github Governor configuration has to be set.");
        this.githubGovernorConfiguration = githubGovernorConfiguration;
    }
    public GithubGovernorStrategy annotation(Github annotation)
    {
        this.annotation = annotation;
        return this;
    }

    public GithubGovernorStrategy issue(Issue githubIssue)
    {
        this.githubIssue = githubIssue;
        return this;
    }

    @Override
    public ExecutionDecision resolve()
    {
        Validate.notNull(githubIssue, "Github issue must be specified.");
        Validate.notNull(annotation, "Annotation must be specified.");
        
        String githubStatus = githubIssue.getState();

        if (githubStatus == null || githubStatus.length() == 0)
        {
            return ExecutionDecision.execute();
        }

        if (annotation.force())
        {
            return ExecutionDecision.execute(FORCING_EXECUTION_REASON_STRING);
        }

        if (githubGovernorConfiguration.getForce())
        {
            return ExecutionDecision.execute(FORCING_EXECUTION_REASON_STRING);
        }

        if (githubStatus.equals(GITHUB_CLOSED_STRING))
        {
            return ExecutionDecision.execute();
        }

        return ExecutionDecision.dontExecute(String.format(SKIPPING_EXECUTION_REASON_STRING, githubIssue.getNumber(), githubStatus));
    }

}
