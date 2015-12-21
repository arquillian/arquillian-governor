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
package org.arquillian.extension.governor.redmine;

import org.arquillian.extension.governor.configuration.GovernorConfiguration;
import org.arquillian.extension.governor.configuration.GovernorConfigurator;
import org.arquillian.extension.governor.impl.GovernorExecutionDecider;
import org.arquillian.extension.governor.impl.GovernorTestClassScanner;
import org.arquillian.extension.governor.redmine.api.Redmine;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfigurator;
import org.arquillian.extension.governor.redmine.impl.RedmineGovernorClient;
import org.arquillian.extension.governor.redmine.impl.RedmineGovernorClientFactory;
import org.arquillian.extension.governor.redmine.impl.RedmineTestExecutionDecider;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.DecideMethodExecutions;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class RedmineGovernorTestCase extends AbstractGovernorTestCase
{

    private static final String DEFAULT_SERVER = "localhost";
    private static final String DEFAULT_APIKEY = "testapikey";

    @Inject
    @ApplicationScoped
    private InstanceProducer<ServiceLoader> serviceProducer;

    @Mock
    private ServiceLoader serviceLoader;

    private GovernorConfiguration governorConfiguration;

    private RedmineGovernorConfiguration redminebGovernorConfiguration;

    private Manager manager;

    private static String apiKey;
    private static String redmineServer;

    private GovernorProvider governorProvider = new GovernorProvider()
    {
        @Override
        public Class<? extends Annotation> provides()
        {
            return Redmine.class;
        }
    };

    @Override
    public void addExtensions(List<Class<?>> extensions)
    {
        extensions.add(RedmineGovernorConfigurator.class);
        extensions.add(RedmineTestExecutionDecider.class);
        extensions.add(GovernorTestClassScanner.class);
        extensions.add(GovernorExecutionDecider.class);
        extensions.add(GovernorConfigurator.class);
    }

    @org.junit.BeforeClass
    public static void setupClass() throws Exception
    {
        redmineServer = resolveServer();
        apiKey = resolveApiKey();
    }

    @Before
    public void setup() throws Exception
    {

        serviceProducer.set(serviceLoader);

        List<GovernorProvider> governorProviders = new ArrayList<GovernorProvider>();
        governorProviders.add(governorProvider);

        Mockito.when(serviceLoader.all(GovernorProvider.class)).thenReturn(governorProviders);

        manager = Mockito.spy(getManager());
        Mockito.when(manager.resolve(ServiceLoader.class)).thenReturn(serviceLoader);

        governorConfiguration = new GovernorConfiguration();
        bind(ApplicationScoped.class, GovernorConfiguration.class, governorConfiguration);

        redminebGovernorConfiguration = new RedmineGovernorConfiguration();
        redminebGovernorConfiguration.setApiKey("apikey");
        redminebGovernorConfiguration.setServer("http://localhost");
        redminebGovernorConfiguration.setForce(true);
        redminebGovernorConfiguration.setClosePassed(true);

        bind(ApplicationScoped.class, RedmineGovernorConfiguration.class, redminebGovernorConfiguration);

        RedmineGovernorClient redmineGovernorClient = new RedmineGovernorClientFactory().build(redminebGovernorConfiguration);
        bind(ApplicationScoped.class, RedmineGovernorClient.class, redmineGovernorClient);
    }

    @Test
    public void gitHubGovernorTest()
    {
        fire(new BeforeClass(FakeTestClass.class));

        assertEventFired(BeforeClass.class, 1);
        assertEventFired(DecideMethodExecutions.class, 1);

        GovernorConfiguration configuration = manager.getContext(ApplicationContext.class).getObjectStore().get(GovernorConfiguration.class);
        assertThat(configuration, is(not(nullValue())));

        RedmineGovernorConfiguration gitHubConfiguration = manager.getContext(ApplicationContext.class).getObjectStore().get(RedmineGovernorConfiguration.class);
        assertThat(gitHubConfiguration, is(not(nullValue())));

        // for every method and for every Governor annotation of that method
        assertEventFired(ExecutionDecisionEvent.class, 1);

        ExecutionDecision decision = manager.getContext(ClassContext.class).getObjectStore().get(ExecutionDecision.class);

        assertThat(decision, is(not(nullValue())));
        assertEquals(decision.getDecision(), Decision.EXECUTE);
    }

    // utils

    private static final class FakeTestClass
    {
        @Test
        @Redmine("1")
        public void fakeTest()
        {
        }

        @Test
        public void someTestMethod()
        {
        }
    }

    // helpers


    private static String resolveServer()
    {
        String redmineServerAddressProperty = System.getProperty("redmine.governor.server");

        if (redmineServerAddressProperty == null || redmineServerAddressProperty.isEmpty())
        {
            redmineServerAddressProperty = DEFAULT_SERVER;
        }

        return redmineServerAddressProperty;
    }


    private static String resolveApiKey()
    {
        String redmineApiKeyProperty = System.getProperty("redmine.governor.apikey");

        if (redmineApiKeyProperty == null || redmineApiKeyProperty.isEmpty())
        {
            redmineApiKeyProperty = DEFAULT_APIKEY;
        }

        return redmineApiKeyProperty;
    }
    

}
