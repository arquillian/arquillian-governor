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

import java.net.URI;

import org.arquillian.extension.governor.api.GovernorClientFactory;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JiraGovernorClientFactory implements GovernorClientFactory<JiraGovernorConfiguration, JiraGovernorClient>
{

    private JiraGovernorConfiguration jiraGovernorConfiguration = null;

    @Override
    public JiraGovernorClient build(JiraGovernorConfiguration jiraGovernorConfiguration) throws Exception
    {
        Validate.notNull(jiraGovernorConfiguration, "Jira governor configuration has to be set.");
        this.jiraGovernorConfiguration = jiraGovernorConfiguration;

        final URI jiraServerUri = this.jiraGovernorConfiguration.getServerURI();
        final String username = this.jiraGovernorConfiguration.getUsername();
        final String password = this.jiraGovernorConfiguration.getPassword();

        final AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, username, password);

        final JiraGovernorClient client = new JiraGovernorClient();
        client.setConfiguration(this.jiraGovernorConfiguration);
        client.initializeRestClient(restClient);
        client.setGovernorStrategy(new JiraGovernorStrategy(jiraGovernorConfiguration));

        return client;
    }

}
