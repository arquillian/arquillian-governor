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

import org.arquillian.extension.governor.api.GovernorClientFactory;
import org.arquillian.extension.governor.github.configuration.GithubGovernorConfiguration;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GithubGovernorClientFactory implements GovernorClientFactory<GithubGovernorConfiguration, GithubGovernorClient>
{

    private GithubGovernorConfiguration githubGovernorConfiguration;

    @Override
    public GithubGovernorClient build(
            GithubGovernorConfiguration governorConfiguration) throws Exception 
    {
        Validate.notNull(governorConfiguration, "Github governor configuration has to be set.");
        this.githubGovernorConfiguration = governorConfiguration;

        GitHubClient gitHubClient = new GitHubClient();
        if(this.githubGovernorConfiguration.getUsername() != null && this.githubGovernorConfiguration.getUsername().length() > 0 && this.githubGovernorConfiguration.getPassword() != null && this.githubGovernorConfiguration.getPassword().length() > 0) {
            gitHubClient.setCredentials(this.githubGovernorConfiguration.getUsername(), this.githubGovernorConfiguration.getPassword());
        }

        if(this.githubGovernorConfiguration.getToken() != null && this.githubGovernorConfiguration.getToken().length() > 0) {
            gitHubClient.setOAuth2Token(githubGovernorConfiguration.getToken());
        }

        GithubGovernorClient githubGovernorClient = new GithubGovernorClient(gitHubClient, githubGovernorConfiguration);
        githubGovernorClient.setGovernorStrategy(new GithubGovernorStrategy(this.githubGovernorConfiguration));

        return githubGovernorClient;
    }

}
