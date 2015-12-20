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

import com.taskadapter.redmineapi.bean.Issue;
import org.arquillian.extension.governor.api.GovernorStrategy;
import org.arquillian.extension.governor.redmine.api.IssueStatus;
import org.arquillian.extension.governor.redmine.api.Redmine;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 *
 */
public class RedmineGovernorStrategy implements GovernorStrategy
{
    public static final String FORCING_EXECUTION_REASON_STRING = "forcing execution";
    public static final String SKIPPING_EXECUTION_REASON_STRING = "Skipping %s. Status %s.";

    private RedmineGovernorConfiguration redmineGovernorConfiguration;
    private Redmine annotation;
    private Issue redmineIssue;

    public RedmineGovernorStrategy(RedmineGovernorConfiguration redmineGovernorConfiguration)
    {
        Validate.notNull(redmineGovernorConfiguration, "Redmine Governor configuration has to be set.");
        this.redmineGovernorConfiguration = redmineGovernorConfiguration;
    }
    public RedmineGovernorStrategy annotation(Redmine annotation)
    {
        this.annotation = annotation;
        return this;
    }

    public RedmineGovernorStrategy issue(Issue redmine)
    {
        this.redmineIssue = redmine;
        return this;
    }

    @Override
    public ExecutionDecision resolve()
    {
        Validate.notNull(redmineIssue, "Redmine issue must be specified.");
        Validate.notNull(annotation, "Annotation must be specified.");

        Integer issueStatus = redmineIssue.getStatusId();


        if (issueStatus == null || IssueStatus.isClosed(issueStatus))
        {
            if(annotation.openFailed()){
                //if issue is closed and test fails, governor will reopen issue
                return ExecutionDecision.execute(FORCING_EXECUTION_REASON_STRING);
            }

            return ExecutionDecision.execute();
        }

        if (annotation.force() || redmineGovernorConfiguration.getForce())
        {
            return ExecutionDecision.execute(FORCING_EXECUTION_REASON_STRING);
        }


        return ExecutionDecision.dontExecute(String.format(SKIPPING_EXECUTION_REASON_STRING, redmineIssue.getId(), issueStatus));
    }

}
