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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.arquillian.extension.governor.api.Governor;
import org.arquillian.extension.governor.api.GovernorRegistry;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class GovernorRegistryImpl implements GovernorRegistry
{
    private Map<Method, List<Annotation>> scannedTestMethods = new HashMap<Method, List<Annotation>>();

    public void put(final Map<Method, List<Annotation>> scannedTestMethods)
    {
        Validate.notNull(scannedTestMethods, "Scanned test methods map must be specified.");
        this.scannedTestMethods = scannedTestMethods;
    }

    @Override
    public Map<Method, List<Annotation>> get()
    {
        return Collections.unmodifiableMap(scannedTestMethods);
    }

    @Override
    public List<Method> getMethodsForAnnotation(Class<? extends Annotation> annotationClass)
    {
        Validate.notNull(annotationClass, "An annotation class to get methods for must be specified.");

        if (annotationClass.getAnnotation(Governor.class) == null) {
            throw new IllegalStateException("Annotation class to get methods for is not annotated with Governor class.");
        }

        final List<Method> methods = new ArrayList<Method>();

        for (final Map.Entry<Method, List<Annotation>> entry : scannedTestMethods.entrySet())
        {
            for (final Annotation methodAnnotation : entry.getValue())
            {
                if (methodAnnotation.annotationType() == annotationClass)
                {
                    methods.add(entry.getKey());
                    break;
                }
            }
        }

        return methods;
    }
}
