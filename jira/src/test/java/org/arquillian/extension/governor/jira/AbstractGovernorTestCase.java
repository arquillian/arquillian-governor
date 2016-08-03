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
package org.arquillian.extension.governor.jira;

import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.context.Context;
import org.jboss.arquillian.core.test.AbstractManagerTestBase;
import org.jboss.arquillian.test.impl.context.ClassContextImpl;
import org.jboss.arquillian.test.impl.context.SuiteContextImpl;
import org.jboss.arquillian.test.impl.context.TestContextImpl;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.SuiteContext;
import org.jboss.arquillian.test.spi.context.TestContext;

import java.util.List;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public abstract class AbstractGovernorTestCase extends AbstractManagerTestBase {
    @Override
    protected void addContexts(List<Class<? extends Context>> contexts) {
        super.addContexts(contexts);
        contexts.add(SuiteContextImpl.class);
        contexts.add(ClassContextImpl.class);
        contexts.add(TestContextImpl.class);
    }

    @Override
    protected void startContexts(Manager manager) {
        super.startContexts(manager);
        manager.getContext(SuiteContext.class).activate();
        manager.getContext(ClassContext.class).activate(super.getClass());
        manager.getContext(TestContext.class).activate(this);
    }
}
