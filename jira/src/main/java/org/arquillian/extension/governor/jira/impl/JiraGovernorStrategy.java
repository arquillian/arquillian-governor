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

import org.arquillian.extension.governor.api.GovernorStrategy;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

import com.atlassian.jira.rest.client.api.domain.Issue;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JiraGovernorStrategy implements GovernorStrategy
{
    private final JiraGovernorConfiguration jiraGovernorConfiguration;

    private Issue jiraIssue;

    private Jira annotation;

    public static final String FORCING_EXECUTION_REASON_STRING = "forcing execution";

    public static final String SKIPPING_EXECUTION_REASON_STRING = "Skipping %s. Status %s.";

    public static final String JIRA_CLOSED_STRING = "Closed";

    public static final String JIRA_RESOLVED_STRING = "Resolved";

    public JiraGovernorStrategy(JiraGovernorConfiguration jiraGovernorConfiguration)
    {
        Validate.notNull(jiraGovernorConfiguration, "Jira Governor configuration has to be set.");
        this.jiraGovernorConfiguration = jiraGovernorConfiguration;
    }

    public JiraGovernorStrategy issue(Issue jiraIssue)
    {
        this.jiraIssue = jiraIssue;
        return this;
    }

    public JiraGovernorStrategy annotation(Jira annotation)
    {
        this.annotation = annotation;
        return this;
    }

    @Override
    public ExecutionDecision resolve()
    {
        Validate.notNull(jiraIssue, "Jira issue must be specified.");
        Validate.notNull(annotation, "Annotation must be specified.");

        String jiraStatus = jiraIssue.getStatus().getName();

        if (jiraStatus == null || jiraStatus.length() == 0)
        {
            return ExecutionDecision.execute();
        }

        if (annotation.force())
        {
            return ExecutionDecision.execute(FORCING_EXECUTION_REASON_STRING);
        }

        if (jiraGovernorConfiguration.getForce())
        {
            return ExecutionDecision.execute(FORCING_EXECUTION_REASON_STRING);
        }

        if (jiraStatus.equals(JIRA_CLOSED_STRING) || jiraStatus.equals(JIRA_RESOLVED_STRING))
        {
            return ExecutionDecision.execute();
        }

        return ExecutionDecision.dontExecute(String.format(SKIPPING_EXECUTION_REASON_STRING, jiraIssue.getKey(), jiraStatus));
    }

}
