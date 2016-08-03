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
package org.arquillian.extension.governor.ignore;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.test.impl.execution.event.ExecutionEvent;
import org.jboss.arquillian.container.test.impl.execution.event.LocalExecutionEvent;
import org.jboss.arquillian.container.test.impl.execution.event.RemoteExecutionEvent;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.annotation.TestScoped;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * /**
 * Extension that overrides After/Before/Test execution of methods
 * based on regexp or methods set configured in arquillian.xml
 * <p/>
 * <arquillian>
 * <extension qualifier="governor-ignore">
 * <property name="expression">.*</property>
 * <property name="methods">org.acme.foo.ListTest#testStrFilter,org.bar.boo.QwertTest#testFoo</property>
 * <property name="methods_1">org.acme.foo.ListTest#testIntFilter</property>
 * </extension>
 * </arquillian>
 *
 * @author Aslak Knutsen
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class IgnoreObserver {
    public static final String EXTENSION_NAME = "governor-ignore";
    public static final String EXTENSION_PROPERTY_EXP = "expression";
    public static final String EXTENSION_PROPERTY_METHODS = "methods";
    public static final String EXTENSION_IGNORE_NONE = "IGNORE_NONE";
    private final Logger log = Logger.getLogger(IgnoreObserver.class.getName());
    @Inject
    private Instance<ArquillianDescriptor> desciptorInst;

    @Inject
    @TestScoped
    private InstanceProducer<TestResult> testResultProducer;

    private String expression = null;
    private Set<String> methods;

    public void localTest(@Observes(precedence = 1000) EventContext<LocalExecutionEvent> context) {
        execute(context, "local");
    }

    public void remoteTest(@Observes(precedence = 1000) EventContext<RemoteExecutionEvent> context) {
        execute(context, "remote");
    }

    private void execute(EventContext<? extends ExecutionEvent> context, String phase) {
        if (shouldPerformExecution(context.getEvent())) {
            context.proceed();
        } else {
            log.info("Ignore test [" + phase + "]: " + toFqn(context.getEvent()));
            testResultProducer.set(TestResult.skipped(null));
        }
    }

    private boolean shouldPerformExecution(ExecutionEvent event) {
        return !shouldCancelExecution(event);
    }

    private boolean shouldCancelExecution(ExecutionEvent event) {
        final String fqn = toFqn(event);
        return fqn.matches(getExpression()) || getMethods().contains(fqn);
    }

    private String toFqn(ExecutionEvent event) {
        final TestMethodExecutor executor = event.getExecutor();
        final Object test = executor.getInstance();
        final Method method = executor.getMethod();
        return test.getClass().getName() + "#" + method.getName();
    }

    private String getExpression() {
        if (expression == null) {
            String tmp = readExpressionFromConfiguration();
            if (tmp == null) {
                tmp = EXTENSION_IGNORE_NONE;
            }
            expression = tmp;
        }
        return expression;
    }

    private Set<String> getMethods() {
        if (methods == null) {
            methods = readMethodsFromConfiguration();
        }
        return methods;
    }

    private String readExpressionFromConfiguration() {
        final ArquillianDescriptor descriptor = desciptorInst.get();
        for (final ExtensionDef def : descriptor.getExtensions()) {
            if (def.getExtensionName().equalsIgnoreCase(EXTENSION_NAME)) {
                final String exp = def.getExtensionProperties().get(EXTENSION_PROPERTY_EXP);
                if (exp != null) {
                    return exp;
                }
            }
        }
        return null;
    }

    private Set<String> readMethodsFromConfiguration() {
        final Set<String> set = new HashSet<String>();
        final ArquillianDescriptor descriptor = desciptorInst.get();
        for (final ExtensionDef def : descriptor.getExtensions()) {
            if (def.getExtensionName().equalsIgnoreCase(EXTENSION_NAME)) {
                final Map<String, String> properties = def.getExtensionProperties();

                final String exp = properties.get(EXTENSION_PROPERTY_METHODS);
                if (exp != null) {
                    set.addAll(Arrays.asList(exp.split(",")));
                }

                for (final Map.Entry<String, String> entry : properties.entrySet()) {
                    if (entry.getKey().startsWith(EXTENSION_PROPERTY_METHODS + "_")) {
                        set.add(entry.getValue());
                    }
                }
            }
        }
        return set;
    }
}
