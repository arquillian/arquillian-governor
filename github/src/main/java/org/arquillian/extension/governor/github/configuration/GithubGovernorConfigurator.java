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
package org.arquillian.extension.governor.github.configuration;

import org.arquillian.extension.governor.github.impl.GithubGovernorClient;
import org.arquillian.extension.governor.github.impl.GithubGovernorClientFactory;
import org.arquillian.extension.governor.spi.event.GovernorExtensionConfigured;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 *
 */
public class GithubGovernorConfigurator
{
    private static final Logger logger = Logger.getLogger(GithubGovernorConfigurator.class.getName());

    private static final String EXTENSION_NAME = "governor-github";

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    @ApplicationScoped
    private InstanceProducer<GithubGovernorConfiguration> githubGovernorConfiguration;

    @Inject
    @ApplicationScoped
    private InstanceProducer<GithubGovernorClient> githubGovernorClient;

    @Inject
    @ApplicationScoped
    private InstanceProducer<GitHubClient> githubClient;

    public void onGovernorExtensionConfigured(@Observes GovernorExtensionConfigured event, ArquillianDescriptor arquillianDescriptor) throws Exception
    {
        GithubGovernorConfiguration githubGovernorConfiguration = new GithubGovernorConfiguration();

        for (final ExtensionDef extension : arquillianDescriptor.getExtensions())
        {
            if (extension.getExtensionName().equals(EXTENSION_NAME))
            {
                githubGovernorConfiguration.setConfiguration(extension.getExtensionProperties());
                githubGovernorConfiguration.validate();
                break;
            }
        }

        this.githubGovernorConfiguration.set(githubGovernorConfiguration);

        final GithubGovernorClient githubGovernorClient = new GithubGovernorClientFactory().build(this.githubGovernorConfiguration.get());

        this.githubGovernorClient.set(githubGovernorClient);
        this.githubClient.set(githubGovernorClient.getGithubClient());

        if (logger.isLoggable(Level.INFO))
        {
            System.out.println("Configuration of Arquillian Github extension: ");
            System.out.println(githubGovernorConfiguration.toString());
        }
    }
}
