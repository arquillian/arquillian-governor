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
package org.arquillian.extension.governor.jira.configuration;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.extension.governor.api.GovernorClientRegistry;
import org.arquillian.extension.governor.api.GovernorClientRegistryRegistry;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.impl.JiraGovernorClient;
import org.arquillian.extension.governor.jira.impl.JiraGovernorClientFactory;
import org.arquillian.extension.governor.jira.impl.JiraGovernorConfigurationParser;
import org.arquillian.extension.governor.spi.event.GovernorExtensionConfigured;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class JiraGovernorConfigurator
{
    private static final Logger logger = Logger.getLogger(JiraGovernorConfigurator.class.getName());

    private static final String EXTENSION_NAME = "governor-jira";

    public void onJiraGovernorExtensionConfigured(@Observes GovernorExtensionConfigured event, ArquillianDescriptor arquillianDescriptor) throws Exception
    {
        Map<String, JiraGovernorConfiguration> configurations = new JiraGovernorConfigurationParser(EXTENSION_NAME).parse(arquillianDescriptor);

        GovernorClientRegistry governorClientRegistry = new GovernorClientRegistry();

        for (Map.Entry<String, JiraGovernorConfiguration> entry : configurations.entrySet())
        {
            final JiraGovernorClient jiraGovernorClient = new JiraGovernorClientFactory().build(entry.getValue());

            if (logger.isLoggable(Level.INFO))
            {
                System.out.println(String.format("Configuration of Arquillian JIRA extension %s: ", entry.getKey()));
                System.out.println(entry.getValue());
            }

            governorClientRegistry.add(entry.getKey(), jiraGovernorClient);
        }

        GovernorClientRegistryRegistry.instance().add(Jira.class, governorClientRegistry);
    }
}
