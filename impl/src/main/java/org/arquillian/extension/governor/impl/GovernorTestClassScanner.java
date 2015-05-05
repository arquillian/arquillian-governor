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
package org.arquillian.extension.governor.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.arquillian.extension.governor.api.Governor;
import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.configuration.GovernorConfiguration;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.DecideMethodExecutions;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class GovernorTestClassScanner
{
    @Inject
    @ApplicationScoped
    private InstanceProducer<GovernorRegistry> governorRegistry;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<DecideMethodExecutions> decideMethodExecution;

    @Inject
    private Instance<GovernorConfiguration> governorConfiguration;

    public void onBeforeClass(@Observes BeforeClass event)
    {
        TestMethodExecutionRegister.setConfigration(governorConfiguration.get());
        TestMethodExecutionRegister.clear();

        if (governorConfiguration.get().getIgnore())
        {
            return;
        }

        final Collection<GovernorProvider> governorProviders = serviceLoader.get().all(GovernorProvider.class);

        checkGovernorProviderUniqueness(governorProviders);

        final Map<Method, List<Annotation>> scannedTestMethods = scanTestMethods(event.getTestClass(), Governor.class);

        GovernorRegistryImpl governorRegistry = new GovernorRegistryImpl();
        governorRegistry.put(scannedTestMethods);
        this.governorRegistry.set(governorRegistry);

        decideMethodExecution.fire(new DecideMethodExecutions());
    }

    private void checkGovernorProviderUniqueness(final Collection<GovernorProvider> governorProviders)
    {
        final Set<Class<? extends Annotation>> uniqueProviders = new HashSet<Class<? extends Annotation>>();

        for (final GovernorProvider governorProvider : governorProviders)
        {
            Class<? extends Annotation> governorClass = governorProvider.provides();

            if (governorClass == null) {
                throw new IllegalStateException(
                    String.format("Governor provider's provides() method (%s) returns null object.",
                        governorProvider.getClass().getName()));
            }

            Governor governorAnnotation = governorClass.getAnnotation(Governor.class);

            if (governorAnnotation == null)
            {
                throw new IllegalStateException(
                    String.format("Governor provider (%s) does not provide annotation annotated by Governor class.",
                        governorProvider.getClass().getName()));
            }

            if (!uniqueProviders.add(governorClass))
            {
                throw new IllegalStateException(
                    String.format("You have put on class path providers which provide the same governor annotation (%s).",
                        governorAnnotation.annotationType()));
            }
        }
    }

    private Map<Method, List<Annotation>> scanTestMethods(TestClass testClass, Class<? extends Annotation> governorAnnotation)
    {
        Validate.notNull(testClass, "Test class to scan must be specified.");

        final Map<Method, List<Annotation>> methodAnnotationsMap = new HashMap<Method, List<Annotation>>();

        final Method[] methods = testClass.getJavaClass().getMethods();

        for (final Method method : methods)
        {
            List<Annotation> methodAnnotations = new ArrayList<Annotation>();

            for (final Annotation annotation : method.getAnnotations())
            {
                if (annotation.annotationType().isAnnotationPresent(governorAnnotation))
                {
                    methodAnnotations.add(annotation);
                }
            }

            if (methodAnnotations.size() > 0)
            {
                methodAnnotationsMap.put(method, methodAnnotations);
            }

        }

        return methodAnnotationsMap;
    }
}
