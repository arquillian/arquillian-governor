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

import java.util.Arrays;
import java.util.Collection;

import org.arquillian.extension.governor.api.GovernorClient;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.ServerVersionConstants;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JiraGovernorClient implements GovernorClient<Jira, JiraGovernorStrategy>
{
    private JiraRestClient restClient;
    private JiraGovernorStrategy jiraGovernorStrategy;
    private JiraGovernorConfiguration jiraGovernorConfiguration;

    private int JIRA_BUILD_NUMBER = 0;

    public void setConfiguration(JiraGovernorConfiguration jiraGovernorConfiguration)
    {
        Validate.notNull(jiraGovernorConfiguration, "Jira Governor configuration must be specified.");
        this.jiraGovernorConfiguration = jiraGovernorConfiguration;
    }

    @Override
    public ExecutionDecision resolve(final Jira annotation)
    {
        Validate.notNull(restClient, "Jira REST client must be specified.");
        Validate.notNull(jiraGovernorStrategy, "Governor strategy must be specified. Have you already called setGovernorStrategy()?");

        final String jiraIssueKey = annotation.value();

        if (jiraIssueKey == null || jiraIssueKey.length() == 0)
        {
            return ExecutionDecision.execute();
        }

        final Issue jiraIssue = getIssue(jiraIssueKey);

        // when there is some error while we are getting the issue, we execute that test
        if (jiraIssue == null)
        {
            return ExecutionDecision.execute();
        }

        return jiraGovernorStrategy.annotation(annotation).issue(jiraIssue).resolve();
    }

    @Override
    public void close(String id)
    {
        Validate.notNull(restClient, "Jira REST client must be specified.");

        try
        {
            final Issue issue = restClient.getIssueClient().getIssue(id).get();

            final Iterable<Transition> transitions = restClient.getIssueClient().getTransitions(issue.getTransitionsUri()).claim();
            final Transition resolveIssueTransition = getTransitionByName(transitions, "Resolve Issue");

            final Collection<FieldInput> fieldInputs;

            if (JIRA_BUILD_NUMBER > ServerVersionConstants.BN_JIRA_5)
            {
                fieldInputs = Arrays.asList(new FieldInput("resolution", ComplexIssueInputFieldValue.with("name", "Done")));
            } else {
                fieldInputs = Arrays.asList(new FieldInput("resolution", "Done"));
            }

            final Comment closingMessage = Comment.valueOf(getClosingMessage());
            final TransitionInput transitionInput = new TransitionInput(resolveIssueTransition.getId(), fieldInputs, closingMessage);

            restClient.getIssueClient().transition(issue.getTransitionsUri(), transitionInput).claim();
        } catch (Exception e)
        {
            // error while getting Issue to close, doing nothing
        }
    }

    @Override
    public void setGovernorStrategy(JiraGovernorStrategy jiraGovernorStrategy)
    {
        Validate.notNull(jiraGovernorStrategy, "Jira Governor strategy must be specified.");
        this.jiraGovernorStrategy = jiraGovernorStrategy;
    }

    // not publicly visible helpers

    void initializeRestClient(final JiraRestClient restClient) throws Exception
    {
        Validate.notNull(restClient, "Jira REST client must be specified.");
        this.restClient = restClient;

        JIRA_BUILD_NUMBER = this.restClient.getMetadataClient().getServerInfo().claim().getBuildNumber();
    }

    // private helpers

    private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName)
    {
        for (Transition transition : transitions)
        {
            if (transition.getName().equals(transitionName))
            {
                return transition;
            }
        }

        return null;
    }

    private Issue getIssue(String key)
    {
        try
        {
            return restClient.getIssueClient().getIssue(key).get();
        } catch (Exception e)
        {
            return null;
        }
    }

    private String getClosingMessage()
    {
        Validate.notNull(jiraGovernorConfiguration, "Jira Governor configuration must be set.");

        return String.format(jiraGovernorConfiguration.getClosingMessage(), jiraGovernorConfiguration.getUsername());
    }

}
