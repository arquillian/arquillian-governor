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
package org.arquillian.extension.governor.jira.configuration;

import org.arquillian.extension.governor.jira.impl.JiraGovernorClient;
import org.arquillian.extension.governor.jira.impl.JiraGovernorClientFactory;
import org.arquillian.extension.governor.spi.event.GovernorExtensionConfigured;
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
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class JiraGovernorConfigurator {
    private static final Logger logger = Logger.getLogger(JiraGovernorConfigurator.class.getName());

    private static final String EXTENSION_NAME = "governor-jira";

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    @ApplicationScoped
    private InstanceProducer<JiraGovernorConfiguration> jiraGovernorConfiguration;

    @Inject
    @ApplicationScoped
    private InstanceProducer<JiraGovernorClient> jiraGovernorClient;

    public void onGovernorExtensionConfigured(@Observes GovernorExtensionConfigured event, ArquillianDescriptor arquillianDescriptor) throws Exception {
        final JiraGovernorConfiguration jiraGovernorConfiguration = new JiraGovernorConfiguration();

        for (final ExtensionDef extension : arquillianDescriptor.getExtensions()) {
            if (extension.getExtensionName().equals(EXTENSION_NAME)) {
                jiraGovernorConfiguration.setConfiguration(extension.getExtensionProperties());
                jiraGovernorConfiguration.validate();
                break;
            }
        }

        this.jiraGovernorConfiguration.set(jiraGovernorConfiguration);

        final JiraGovernorClient jiraGovernorClient = new JiraGovernorClientFactory().build(this.jiraGovernorConfiguration.get());

        this.jiraGovernorClient.set(jiraGovernorClient);

        if (logger.isLoggable(Level.INFO)) {
            System.out.println("Configuration of Arquillian JIRA extension: ");
            System.out.println(jiraGovernorConfiguration.toString());
        }
    }
}
