package org.arquillian.extension.governor.jira.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.arquillian.extension.governor.api.GovernorClientRegistryRegistry;
import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.api.Jiras;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;

public class JirasTestExecutionDecider implements TestExecutionDecider, GovernorProvider
{
    private static final Map<Method, Integer> lifecycleCountRegister = new HashMap<Method, Integer>();

    @Inject
    @ClassScoped
    private InstanceProducer<ExecutionDecision> executionDecision;

    @Override
    public Class<? extends Annotation> provides()
    {
        return Jiras.class;
    }

    @Override
    public ExecutionDecision decide(Method testMethod)
    {
        return TestMethodExecutionRegister.resolve(testMethod, provides());
    }

    @Override
    public int precedence()
    {
        return 0;
    }

    public void on(@Observes ExecutionDecisionEvent event)
    {
        ExecutionDecision executionDecision = this.executionDecision.get();

        if (executionDecision == null || executionDecision.getDecision() == Decision.DONT_EXECUTE)
        {
            return;
        }

        if (event.getAnnotation().annotationType() != provides())
        {
            return;
        }

        Jiras jiras = ((Jiras) event.getAnnotation());

        if (jiras.force())
        {
            this.executionDecision.set(ExecutionDecision.execute());
            return;
        }

        for (Jira jiraIssue : ((Jiras) event.getAnnotation()).value())
        {
            JiraGovernorClient governorClient = (JiraGovernorClient) GovernorClientRegistryRegistry
                .instance()
                .get(provides())
                .get(jiraIssue.server());

            ExecutionDecision resolvedExecutionDecision = governorClient.resolve(jiraIssue);

            if (resolvedExecutionDecision.getDecision() == Decision.EXECUTE)
            {
                JiraAnnotationRegister.add(jiraIssue);
                this.executionDecision.set(resolvedExecutionDecision);
                break;
            }
        }
    }
    
    public void on(@Observes Before event, GovernorRegistry governorRegistry)
    {
        if (TestMethodExecutionRegister.resolve(event.getTestMethod(), provides()).getDecision() == Decision.EXECUTE)
        {
            for (Annotation annotation : governorRegistry.getAnnotationsForMethod(event.getTestMethod()))
            {
                if (annotation.annotationType() == provides())
                {
                    for (Jira jiraIssue : ((Jiras) annotation).value())
                    {
                        if (JiraAnnotationRegister.contains(jiraIssue))
                        {
                            jiraAnnotationProducer.set(jiraIssue);       
                        }
                    }
                }
            }
        }
    }
}
