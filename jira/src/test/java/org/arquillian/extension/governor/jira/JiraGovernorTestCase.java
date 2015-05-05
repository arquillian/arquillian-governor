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
package org.arquillian.extension.governor.jira;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.configuration.GovernorConfiguration;
import org.arquillian.extension.governor.configuration.GovernorConfigurator;
import org.arquillian.extension.governor.impl.GovernorExecutionDecider;
import org.arquillian.extension.governor.impl.GovernorTestClassScanner;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfigurator;
import org.arquillian.extension.governor.jira.impl.JiraGovernorClient;
import org.arquillian.extension.governor.jira.impl.JiraGovernorClientFactory;
import org.arquillian.extension.governor.jira.impl.JiraTestExecutionDecider;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * This test is ignored by default, it needs your credentials provided as 'jira.governor.password' and 'jira.governor.username'
 * to your JIRA account, by default on 'https://issues.jboss.org'. Server can be overriden by 'jira.governor.address' property.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore
public class JiraGovernorTestCase extends AbstractGovernorTestCase
{
    private static final String DEFAULT_JIRA_SERVER_ADDRESS = "https://issues.jboss.org";

    private static String JIRA_SERVER_ADDRESS;

    private static String USERNAME;

    private static String PASSWORD;

    @Inject
    @ApplicationScoped
    private InstanceProducer<ServiceLoader> serviceProducer;

    @Mock
    private ServiceLoader serviceLoader;

    private GovernorConfiguration governorConfiguration;

    private JiraGovernorConfiguration jiraGovernorConfiguration;

    private Manager manager;

    private GovernorProvider governorProvider = new GovernorProvider()
    {
        @Override
        public Class<? extends Annotation> provides()
        {
            return Jira.class;
        }
    };

    @Override
    public void addExtensions(List<Class<?>> extensions)
    {
        extensions.add(JiraGovernorConfigurator.class);
        extensions.add(JiraTestExecutionDecider.class);
        extensions.add(GovernorTestClassScanner.class);
        extensions.add(GovernorExecutionDecider.class);
        extensions.add(GovernorConfigurator.class);
    }

    @org.junit.BeforeClass
    public static void setupClass() throws Exception
    {
        JIRA_SERVER_ADDRESS = resolveJiraServerAddress();
        USERNAME = getUsername();
        PASSWORD = getPassword();
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

        jiraGovernorConfiguration = new JiraGovernorConfiguration();
        jiraGovernorConfiguration.setServer(JIRA_SERVER_ADDRESS);
        jiraGovernorConfiguration.setUsername(USERNAME);
        jiraGovernorConfiguration.setPassword(PASSWORD);

        bind(ApplicationScoped.class, JiraGovernorConfiguration.class, jiraGovernorConfiguration);

        JiraGovernorClient jiraGovernorClient = new JiraGovernorClientFactory().build(jiraGovernorConfiguration);
        bind(ApplicationScoped.class, JiraGovernorClient.class, jiraGovernorClient);
    }

    @Test
    public void jiraGovernorTest()
    {
        fire(new BeforeClass(FakeTestClass.class));

        assertEventFired(BeforeClass.class, 1);
        assertEventFired(DecideMethodExecutions.class, 1);

        GovernorRegistry governorRegistry = manager.getContext(ClassContext.class).getObjectStore().get(GovernorRegistry.class);
        assertThat(governorRegistry, is(not(nullValue())));

        GovernorConfiguration configuration = manager.getContext(ApplicationContext.class).getObjectStore().get(GovernorConfiguration.class);
        assertThat(configuration, is(not(nullValue())));

        JiraGovernorConfiguration jiraConfiguration = manager.getContext(ApplicationContext.class).getObjectStore().get(JiraGovernorConfiguration.class);
        assertThat(jiraConfiguration, is(not(nullValue())));

        List<Method> jiraMethods = governorRegistry.getMethodsForAnnotation(Jira.class);
        assertEquals(1, jiraMethods.size());

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
        @Jira("ARQ-1")
        public void fakeTest()
        {
            // this will be run becase it is Closed
        }

        @Test
        public void someTestMethod()
        {
        }
    }

    // helpers

    private static String resolveJiraServerAddress()
    {
        String jiraServerAddressProperty = System.getProperty("jira.governor.address");

        if (jiraServerAddressProperty == null || jiraServerAddressProperty.isEmpty())
        {
            return DEFAULT_JIRA_SERVER_ADDRESS;
        }

        try
        {
            new URI(jiraServerAddressProperty);
            new URL(jiraServerAddressProperty);
        } catch (URISyntaxException ex)
        {
            return DEFAULT_JIRA_SERVER_ADDRESS;
        } catch (MalformedURLException ex) {
            return DEFAULT_JIRA_SERVER_ADDRESS;
        }

        return jiraServerAddressProperty;
    }

    private static String getPassword() throws Exception
    {
        String password = System.getProperty("jira.governor.password");

        if (password == null || password.length() == 0)
        {
            throw new Exception("You have to provide your JIRA password as system property 'jira.governor.password' to this test.");
        }

        return password;
    }

    private static String getUsername() throws Exception
    {
        String username = System.getProperty("jira.governor.username");

        if (username == null || username.length() == 0)
        {
            throw new Exception("You have to provide your JIRA username as system property 'jira.governor.username' to this test.");
        }

        return username;
    }
}
