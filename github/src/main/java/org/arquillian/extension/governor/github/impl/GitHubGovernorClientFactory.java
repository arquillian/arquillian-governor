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
package org.arquillian.extension.governor.github.impl;

import org.arquillian.extension.governor.api.GovernorClientFactory;
import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfiguration;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GitHubGovernorClientFactory implements GovernorClientFactory<GitHubGovernorConfiguration, GitHubGovernorClient>
{

    private GitHubGovernorConfiguration gitHubGovernorConfiguration;

    @Override
    public GitHubGovernorClient build(
            GitHubGovernorConfiguration governorConfiguration) throws Exception
    {
        Validate.notNull(governorConfiguration, "GitHub governor configuration has to be set.");
        this.gitHubGovernorConfiguration = governorConfiguration;

        GitHubClient gitHubClient = new GitHubClient();
        if (this.gitHubGovernorConfiguration.getUsername() != null && this.gitHubGovernorConfiguration.getUsername().length() > 0 && this.gitHubGovernorConfiguration.getPassword() != null && this.gitHubGovernorConfiguration.getPassword().length() > 0) {
            gitHubClient.setCredentials(this.gitHubGovernorConfiguration.getUsername(), this.gitHubGovernorConfiguration.getPassword());
        }

        if (this.gitHubGovernorConfiguration.getToken() != null && this.gitHubGovernorConfiguration.getToken().length() > 0) {
            gitHubClient.setOAuth2Token(gitHubGovernorConfiguration.getToken());
        }

        GitHubGovernorClient gitHubGovernorClient = new GitHubGovernorClient(gitHubClient, gitHubGovernorConfiguration);
        gitHubGovernorClient.setGovernorStrategy(new GitHubGovernorStrategy(this.gitHubGovernorConfiguration));

        return gitHubGovernorClient;
    }

}
