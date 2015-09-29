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
import org.arquillian.extension.governor.github.api.Github;
import org.arquillian.extension.governor.github.configuration.GithubGovernorConfiguration;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GithubGovernorClient implements GovernorClient<Github, GithubGovernorStrategy>
{
    private static final Logger logger = Logger.getLogger(GithubGovernorClient.class.getName());

    private GitHubClient githubClient;
    private GithubGovernorConfiguration githubGovernorConfiguration;
    private IssueService issueService;
    private GithubGovernorStrategy githubGovernorStrategy;

    public GithubGovernorClient(GitHubClient gitHubClient, GithubGovernorConfiguration githubGovernorConfiguration) {
        this.initializeGithubClient(gitHubClient);
        this.setConfiguration(githubGovernorConfiguration);
    }

    @Override
    public ExecutionDecision resolve(Github annotation)
    {
        Validate.notNull(githubClient, "Github REST client must be specified.");
        Validate.notNull(githubGovernorStrategy, "Governor strategy must be specified. Have you already called setGovernorStrategy()?");

        final String githubIssueKey = annotation.value();

        if (githubIssueKey == null || githubIssueKey.length() == 0)
        {
            return ExecutionDecision.execute();
        }

        final Issue githubIssue = getIssue(githubIssueKey);

        // when there is some error while we are getting the issue, we execute that test
        if (githubIssue == null)
        {
            logger.warning(String.format("Github Issue %s couldn't be retrieved from congigured repository.", githubIssueKey));
            return ExecutionDecision.execute();
        }

        return githubGovernorStrategy.annotation(annotation).issue(githubIssue).resolve();
    }

    @Override
    public void close(String issueId) 
    {
        Validate.notNull(githubClient, "Github REST client must be specified.");

        Comment comment = null;
        try
        {
            Issue issue = getIssue(issueId);
            issue.setState(IssueService.STATE_CLOSED);
            comment = this.issueService.createComment(this.githubGovernorConfiguration.getRepositoryUser(), this.githubGovernorConfiguration.getRepository(), issueId, getClosingMessage());
            this.issueService.editIssue(this.githubGovernorConfiguration.getRepositoryUser(), this.githubGovernorConfiguration.getRepository(), issue);
        } catch (Exception e)
        {
            if (comment != null) {
                deleteComment(comment);
            }
            // error while getting Issue to close, doing nothing
            //WARN log
        }
    }

    private void deleteComment(Comment comment) {
        try {
            this.issueService.deleteComment(this.githubGovernorConfiguration.getRepositoryUser(), this.githubGovernorConfiguration.getRepository(), comment.getId());
        } catch (IOException e1) {
        }
    }

    @Override
    public void setGovernorStrategy(GithubGovernorStrategy strategy) 
    {
        Validate.notNull(strategy, "Github Governor strategy must be specified.");
        this.githubGovernorStrategy = strategy;
    }

    public GitHubClient getGithubClient() 
    {
        return githubClient;
    }

    private void setConfiguration(GithubGovernorConfiguration githubGovernorConfiguration)
    {
        Validate.notNull(githubGovernorConfiguration, "Github Governor configuration must be specified.");
        this.githubGovernorConfiguration = githubGovernorConfiguration;
    }

    private void initializeGithubClient(final GitHubClient gitHubClient)
    {
        Validate.notNull(gitHubClient, "Github client must be specified.");
        this.githubClient = gitHubClient;

        this.issueService = new IssueService(this.githubClient);
    }

    private Issue getIssue(String issueNumber) {
        try
        {
            return this.issueService.getIssue(this.githubGovernorConfiguration.getRepositoryUser(), this.githubGovernorConfiguration.getRepository(), issueNumber);
        } catch (Exception e)
        {
            logger.warning(String.format("An exception has occured while getting issue %s. Exception: %s", issueNumber, e.getMessage()));
            return null;
        }
    }
    private String getClosingMessage()
    {
        Validate.notNull(githubGovernorConfiguration, "Github Governor configuration must be set.");

        return String.format(githubGovernorConfiguration.getClosingMessage(), githubGovernorConfiguration.getUsername());
    }

}
