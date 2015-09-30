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

import org.arquillian.extension.governor.api.GovernorClient;
import org.arquillian.extension.governor.github.api.GitHub;
import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfiguration;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GitHubGovernorClient implements GovernorClient<GitHub, GitHubGovernorStrategy>
{
    private static final Logger logger = Logger.getLogger(GitHubGovernorClient.class.getName());

    private GitHubClient gitHubClient;
    private GitHubGovernorConfiguration gitHubGovernorConfiguration;
    private IssueService issueService;
    private GitHubGovernorStrategy gitHubGovernorStrategy;

    public GitHubGovernorClient(GitHubClient gitHubClient, GitHubGovernorConfiguration gitHubGovernorConfiguration) {
        this.initializeGitHubClient(gitHubClient);
        this.setConfiguration(gitHubGovernorConfiguration);
    }

    @Override
    public ExecutionDecision resolve(GitHub annotation)
    {
        Validate.notNull(gitHubClient, "GitHub REST client must be specified.");
        Validate.notNull(gitHubGovernorStrategy, "Governor strategy must be specified. Have you already called setGovernorStrategy()?");

        final String gitHubIssueKey = annotation.value();

        if (gitHubIssueKey == null || gitHubIssueKey.length() == 0)
        {
            return ExecutionDecision.execute();
        }

        final Issue gitHubIssue = getIssue(gitHubIssueKey);

        // when there is some error while we are getting the issue, we execute that test
        if (gitHubIssue == null)
        {
            logger.warning(String.format("GitHub Issue %s couldn't be retrieved from configured repository.", gitHubIssueKey));
            return ExecutionDecision.execute();
        }

        return gitHubGovernorStrategy.annotation(annotation).issue(gitHubIssue).resolve();
    }

    @Override
    public void close(String issueId)
    {
        Validate.notNull(gitHubClient, "GitHub REST client must be specified.");

        Comment comment = null;

        try
        {
            Issue issue = getIssue(issueId);
            issue.setState(IssueService.STATE_CLOSED);
            comment =
                this.issueService.createComment(this.gitHubGovernorConfiguration.getRepositoryUser(), this.gitHubGovernorConfiguration.getRepository(), issueId,
                    getClosingMessage());
            this.issueService.editIssue(this.gitHubGovernorConfiguration.getRepositoryUser(), this.gitHubGovernorConfiguration.getRepository(), issue);
        } catch (Exception e)
        {
            if (comment != null) {
                deleteComment(comment);
            }

            logger.warning(String.format("An exception has occured while closing the issue %s. Exception: %s", issueId, e.getMessage()));
        }
    }

    private void deleteComment(Comment comment) {
        try {
            this.issueService.deleteComment(this.gitHubGovernorConfiguration.getRepositoryUser(), this.gitHubGovernorConfiguration.getRepository(), comment.getId());
        } catch (IOException e1) {
        }
    }

    @Override
    public void setGovernorStrategy(GitHubGovernorStrategy strategy)
    {
        Validate.notNull(strategy, "GitHub Governor strategy must be specified.");
        this.gitHubGovernorStrategy = strategy;
    }

    public GitHubClient getGitHubClient()
    {
        return gitHubClient;
    }

    private void setConfiguration(GitHubGovernorConfiguration gitHubGovernorConfiguration)
    {
        Validate.notNull(gitHubGovernorConfiguration, "GitHub Governor configuration must be specified.");
        this.gitHubGovernorConfiguration = gitHubGovernorConfiguration;
    }

    private void initializeGitHubClient(final GitHubClient gitHubClient)
    {
        Validate.notNull(gitHubClient, "GitHub client must be specified.");
        this.gitHubClient = gitHubClient;

        this.issueService = new IssueService(this.gitHubClient);
    }

    private Issue getIssue(String issueNumber) {
        try
        {
            return this.issueService.getIssue(this.gitHubGovernorConfiguration.getRepositoryUser(), this.gitHubGovernorConfiguration.getRepository(), issueNumber);
        } catch (Exception e)
        {
            logger.warning(String.format("An exception has occured while getting the issue %s. Exception: %s", issueNumber, e.getMessage()));
            return null;
        }
    }

    private String getClosingMessage()
    {
        Validate.notNull(gitHubGovernorConfiguration, "GitHub Governor configuration must be set.");

        String username = gitHubGovernorConfiguration.getUsername();

        if (username == null || username.isEmpty()) {
            username = "unknown";
        }
        
        return String.format(gitHubGovernorConfiguration.getClosingMessage(), username);
    }

}
