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
import java.util.List;
import java.util.Map;

import org.arquillian.extension.governor.api.GovernorRegistry;
import org.arquillian.extension.governor.spi.event.DecideMethodExecutions;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class GovernorExecutionDecider
{
    @Inject
    private Instance<GovernorRegistry> governorRegistry;

    @Inject
    @ClassScoped
    private InstanceProducer<ExecutionDecision> executionDecisionProducer;

    @Inject
    private Instance<ExecutionDecision> executionDecision;

    @Inject
    private Event<ExecutionDecisionEvent> executionDecisionEvent;

    public void on(@Observes DecideMethodExecutions decodeMethodExecution)
    {
        for (Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().get().entrySet())
        {
            final Method testMethod = entry.getKey();

            executionDecisionProducer.set(ExecutionDecision.execute());

            for (final Annotation annotation : entry.getValue())
            {
                executionDecisionEvent.fire(new ExecutionDecisionEvent(annotation));

                // we get here after all TestExecutionDeciders which observe above event are treated
                // and eventually set final execution decision about that annotation
                ExecutionDecision decision = this.executionDecision.get();

                if (decision == null)
                {
                    decision = ExecutionDecision.execute();
                }

                TestMethodExecutionRegister.put(testMethod.toString(), annotation.annotationType(), decision);
            }
        }
    }

}
