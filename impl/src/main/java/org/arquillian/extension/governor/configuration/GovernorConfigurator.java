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
package org.arquillian.extension.governor.configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.extension.governor.api.GovernorConfigurationException;
import org.arquillian.extension.governor.spi.event.GovernorExtensionConfigured;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class GovernorConfigurator
{
    private static final Logger logger = Logger.getLogger(GovernorConfigurator.class.getName());

    private static final String EXTENSION_NAME = "governor";

    @Inject
    @ApplicationScoped
    private InstanceProducer<GovernorConfiguration> governorConfiguration;

    @Inject
    private Event<GovernorExtensionConfigured> governorExtensionConfiguredEvent;

    public void onArquillianDescriptor(@Observes ArquillianDescriptor arquillianDescriptor) throws GovernorConfigurationException {

        GovernorConfiguration governorConfiguration = new GovernorConfiguration();

        for (final ExtensionDef extension : arquillianDescriptor.getExtensions())
        {
            if (extension.getExtensionName().equals(EXTENSION_NAME))
            {
                governorConfiguration.setConfiguration(extension.getExtensionProperties());
                governorConfiguration.validate();
                break;
            }
        }

        this.governorConfiguration.set(governorConfiguration);

        if (logger.isLoggable(Level.INFO))
        {
            System.out.println("Configuration of Arquillian Governor extension: ");
            System.out.println(governorConfiguration.toString());
        }

        governorExtensionConfiguredEvent.fire(new GovernorExtensionConfigured());
    }
}
