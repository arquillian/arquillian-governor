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
package org.arquillian.extension.governor.redmine.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.extension.governor.api.GovernorClient;
import org.arquillian.extension.governor.redmine.api.IssueStatus;
import org.arquillian.extension.governor.redmine.api.Redmine;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.User;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 *
 */
public class RedmineGovernorClient implements GovernorClient<Redmine, RedmineGovernorStrategy>
{
    private static final Logger logger = Logger.getLogger(RedmineGovernorClient.class.getName());

    private RedmineManager redmineManager;
    private RedmineGovernorConfiguration redmineGovernorConfiguration;
    private RedmineGovernorStrategy redmineGovernorStrategy;

    public RedmineGovernorClient(RedmineGovernorConfiguration redmineGovernorConfiguration)
    {
        this.initializeRedmineManager(redmineGovernorConfiguration.getServer(), redmineGovernorConfiguration.getApiKey());
        this.setConfiguration(redmineGovernorConfiguration);
    }

    @Override
    public ExecutionDecision resolve(Redmine annotation)
    {
        Validate.notNull(redmineManager, "Redmine manager must be specified.");
        Validate.notNull(redmineGovernorStrategy, "Governor strategy must be specified. Have you already called setGovernorStrategy()?");

        final String redmineIssueKey = annotation.value();

        if (redmineIssueKey == null || redmineIssueKey.length() == 0)
        {
            return ExecutionDecision.execute();
        }

        final Issue redmineIssue = getIssue(redmineIssueKey);

        // when there is some error while we are getting the issue, we execute that test
        if (redmineIssue == null)
        {
            logger.warning(String.format("Redmine Issue %s couldn't be retrieved from configured repository.", redmineIssueKey));
            return ExecutionDecision.execute();
        }

        return redmineGovernorStrategy.annotation(annotation).issue(redmineIssue).resolve();
    }

    @Override
    public void close(String issueId)
    {
        Validate.notNull(redmineManager, "Redmine manager must be specified.");

        try
        {
            Issue issue = getIssue(issueId);
            if (!IssueStatus.isClosed(issue.getStatusId()))
            {
                if (redmineGovernorConfiguration.getCloseOrder() != null && redmineGovernorConfiguration.getCloseOrder().length() > 0)
                {
                    resolveIntermediateIssueTransitions(issue, redmineGovernorConfiguration.getCloseOrder());
                }
                issue.setStatusId(IssueStatus.CLOSED.getStatusCode());
                issue.setNotes(getClosingMessage());
                redmineManager.getIssueManager().update(issue);
                boolean stillNotClosed = !IssueStatus.isClosed(getIssue(issueId).getStatusId());
                if (stillNotClosed)
                {
                    printAvailableStatus();
                    throw new RuntimeException("Arquillian governor redmine could not close issue. "
                        + "The status transition is probably invalid. Use property 'closeOrder' in arquillian.xml and provide a valid status transition for this issue.");
                }
            }
        } catch (Exception e)
        {
            logger.warning(String.format("An exception has occurred while closing the issue %s. Exception: %s", issueId, e.getMessage()));
        }
    }

    private void printAvailableStatus()
    {
        try
        {
            List<com.taskadapter.redmineapi.bean.IssueStatus> statuses = redmineManager.getIssueManager().getStatuses();
            logger.info("Printing available issue status (id - name):");
            for (com.taskadapter.redmineapi.bean.IssueStatus status : statuses)
            {
                logger.info(status.getId() + " - " + status.getName());
            }
        } catch (RedmineException e)
        {
            logger.log(Level.SEVERE, "Could not list issue statuses.", e);
            e.printStackTrace();
        }

    }

    private void resolveIntermediateIssueTransitions(Issue issue, String closeOrder)
    {
        String[] statusId = closeOrder.split(",");

        int i = 0;
        while (i < statusId.length)
        {
            try
            {
                Integer intermediateStatus = Integer.parseInt(statusId[i].trim());
                if (!IssueStatus.isClosed(intermediateStatus))
                {
                    issue.setStatusId(intermediateStatus);
                    redmineManager.getIssueManager().update(issue);
                }
            } catch (Exception e)
            {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, String.format("Could not update issue %s with status id %d", issue.getId(), statusId[i]), e);
            }
            i++;
        }
    }

    public void open(String issueId, Throwable cause)
    {
        Validate.notNull(redmineManager, "Redmine manager must be specified.");

        try
        {
            Issue issue = getIssue(issueId);
            if (IssueStatus.isClosed(issue.getStatusId()))
            {
                issue.setStatusId(IssueStatus.NEW.getStatusCode());
                StringBuilder openingMessage = new StringBuilder(getOpeningMessage() + "\n");
                openingMessage.append(getCauseAsString(cause));
                issue.setNotes(openingMessage.toString());
                redmineManager.getIssueManager().update(issue);
                boolean stillClosed = IssueStatus.isClosed(getIssue(issueId).getStatusId());
                if (stillClosed)
                {
                    throw new RuntimeException("Arquillian governor redmine could not open issue " + issueId
                        + ". Please check if provided user has privileges for re opening issues.");
                }
            }

        } catch (Exception e)
        {
            logger.warning(String.format("An exception has occurred while closing the issue %s. Exception: %s", issueId, e.getMessage()));
        }
    }

    private String getCauseAsString(Throwable cause)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        cause.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    @Override
    public void setGovernorStrategy(RedmineGovernorStrategy strategy)
    {
        Validate.notNull(strategy, "Redmine Governor strategy must be specified.");
        this.redmineGovernorStrategy = strategy;
    }

    public RedmineManager getRedmineManager()
    {
        return redmineManager;
    }

    private void setConfiguration(RedmineGovernorConfiguration redmineGovernorConfiguration)
    {
        Validate.notNull(redmineGovernorConfiguration, "Redmine Governor configuration must be specified.");
        this.redmineGovernorConfiguration = redmineGovernorConfiguration;
    }

    private void initializeRedmineManager(String uri, String apiKey)
    {
        Validate.notNullOrEmpty(uri, "Redmine uri must be specified.");
        Validate.notNullOrEmpty(apiKey, "User apikey must be provided.");

        redmineManager = RedmineManagerFactory.createWithApiKey(uri, apiKey);
    }

    private Issue getIssue(String issueId)
    {
        try
        {
            if (issueId == null || !isNumeric(issueId))
            {
                throw new IllegalArgumentException("Issue id is invalid.");
            }
            return redmineManager.getIssueManager().getIssueById(new Integer(issueId), Include.journals);
        } catch (Exception e)
        {
            logger.warning(String.format("An exception has occured while getting the issue %s. Exception: %s", issueId, e.getMessage()));
            return null;
        }
    }

    private boolean isNumeric(String issueId)
    {
        try
        {
            Integer.parseInt(issueId);
        } catch (NumberFormatException nfe)
        {
            return false;
        }
        return true;

    }

    private String getClosingMessage()
    {
        Validate.notNull(redmineGovernorConfiguration, "Redmine Governor configuration must be set.");

        String username = null;
        try
        {
            User apiKeyUser = redmineManager.getUserManager().getCurrentUser();
            username = apiKeyUser.getLogin();
        } catch (RedmineException e)
        {
            logger.log(Level.WARNING, "Could not get redmine user.", e);
        }

        if (username == null || username.isEmpty())
        {
            username = "unknown";
        }

        return String.format(redmineGovernorConfiguration.getClosingMessage(), username);
    }

    private String getOpeningMessage()
    {
        Validate.notNull(redmineGovernorConfiguration, "Redmine Governor configuration must be set.");

        String username = null;
        try
        {
            User apiKeyUser = redmineManager.getUserManager().getCurrentUser();
            username = apiKeyUser.getLogin();
        } catch (RedmineException e)
        {
            logger.log(Level.WARNING, "Could not get redmine user.", e);
        }

        if (username == null || username.isEmpty())
        {
            username = "unknown";
        }

        return String.format(redmineGovernorConfiguration.getOpeningMessage(), username);
    }

}
