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

import org.arquillian.extension.governor.api.GovernorClient;
import org.arquillian.extension.governor.api.GovernorClientRegistry;
import org.arquillian.extension.governor.api.GovernorClientRegistryRegistry;
import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.configuration.GovernorConfiguration;
import org.arquillian.extension.governor.configuration.GovernorConfigurator;
import org.arquillian.extension.governor.impl.GovernorExecutionDecider;
import org.arquillian.extension.governor.impl.GovernorTestClassScanner;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister.MethodExecutionDecision;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.api.Jiras;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfigurator;
import org.arquillian.extension.governor.jira.impl.JiraGovernorClient;
import org.arquillian.extension.governor.jira.impl.JiraTestExecutionDecider;
import org.arquillian.extension.governor.jira.impl.JirasTestExecutionDecider;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.DecideMethodExecutions;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JirasGovernorTestCase extends AbstractGovernorTestCase
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

    private GovernorProvider jiraGovernorProvider = new GovernorProvider()
    {
        @Override
        public Class<? extends Annotation> provides()
        {
            return Jira.class;
        }
    };

    private GovernorProvider jirasGovernorProvider = new GovernorProvider()
    {
        @Override
        public Class<? extends Annotation> provides()
        {
            return Jiras.class;
        }
    };

    @Override
    public void addExtensions(List<Class<?>> extensions)
    {
        extensions.add(JiraGovernorConfigurator.class);
        extensions.add(JiraTestExecutionDecider.class);
        extensions.add(JirasTestExecutionDecider.class);
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
        governorProviders.add(jiraGovernorProvider);
        governorProviders.add(jirasGovernorProvider);

        Mockito.when(serviceLoader.all(GovernorProvider.class)).thenReturn(governorProviders);

        manager = Mockito.spy(getManager());
        Mockito.when(manager.resolve(ServiceLoader.class)).thenReturn(serviceLoader);

        governorConfiguration = new GovernorConfiguration();
        bind(ApplicationScoped.class, GovernorConfiguration.class, governorConfiguration);

        GovernorClientRegistry registry = new GovernorClientRegistry();

        registry.add("default", getGovernorClient());
        registry.add("another", getGovernorClient());

        GovernorClientRegistryRegistry.instance().add(Jira.class, registry);
    }

    @Test
    public void jiraGovernorTest()
    {
        fire(new BeforeClass(FakeTestClass.class));

        assertEventFired(BeforeClass.class, 1);
        assertEventFired(DecideMethodExecutions.class, 1);

        GovernorRegistry governorRegistry = manager.getContext(ClassContext.class).getObjectStore().get(GovernorRegistry.class);
        assertThat(governorRegistry, is(not(nullValue())));

        List<Method> jiraMethods = governorRegistry.getMethodsForAnnotation(Jira.class);
        assertEquals(1, jiraMethods.size());

        List<Method> jirasMethods = governorRegistry.getMethodsForAnnotation(Jiras.class);
        assertEquals(1, jirasMethods.size());

        // for every method and for every Governor annotation of that method
        assertEventFired(ExecutionDecisionEvent.class, 2);

        for (MethodExecutionDecision methodExecutionDecision : TestMethodExecutionRegister.getAll())
        {
            assertThat(methodExecutionDecision, is(not(nullValue())));
            assertEquals(methodExecutionDecision.getExecutionDecision(), Decision.EXECUTE);
        }
    }

    // utils

    private static final class FakeTestClass
    {
        @Test
        @Jiras({
            @Jira(value = "ARQ-1"),
            @Jira(value = "ARQ-2", server = "another")
        })
        public void jirasTest()
        {
            // this will be run becase it is Closed
        }

        @Test
        @Jira(value = "ARQ-1", server = "another")
        public void jiraServerTest()
        {
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
        } catch (MalformedURLException ex)
        {
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

    private GovernorClient<?, ?> getGovernorClient()
    {
        jiraGovernorConfiguration = new JiraGovernorConfiguration();
        jiraGovernorConfiguration.setServer(JIRA_SERVER_ADDRESS);
        jiraGovernorConfiguration.setUsername(USERNAME);
        jiraGovernorConfiguration.setPassword(PASSWORD);

        JiraGovernorClient client = new JiraGovernorClient();
        client.setConfiguration(jiraGovernorConfiguration);

        return client;
    }

}
