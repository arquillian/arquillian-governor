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
package org.arquillian.extension.governor.skipper.impl;

import org.arquillian.extension.governor.impl.TestMethodExecutionRegister;
import org.arquillian.extension.governor.skipper.api.Status;
import org.arquillian.extension.governor.skipper.api.TestSpec;
import org.arquillian.extension.governor.spi.GovernorProvider;
import org.arquillian.extension.governor.spi.event.ExecutionDecisionEvent;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision;
import org.jboss.arquillian.test.spi.execution.ExecutionDecision.Decision;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class SkipperTestExecutionDecider implements TestExecutionDecider, GovernorProvider {

    @Inject
    @ClassScoped
    private InstanceProducer<ExecutionDecision> executionDecision;

    @Inject
    private Instance<SkipperReportHolder> holder;

    @Override
    public ExecutionDecision decide(Method testMethod) {
        return TestMethodExecutionRegister.resolve(testMethod, provides());
    }

    @Override
    public Class<? extends Annotation> provides() {
        return TestSpec.class;
    }

    public void on(@Observes ExecutionDecisionEvent event) {
        final ExecutionDecision executionDecision = this.executionDecision.get();

        if (executionDecision == null || executionDecision.getDecision() == Decision.DONT_EXECUTE) {
            return;
        }

        if (event.getAnnotation().annotationType() == provides()) {
            final TestSpec testSpec = (TestSpec) event.getAnnotation();

            final ExecutionDecision decision;

            if (testSpec.status() == Status.AUTOMATED) {
                decision = ExecutionDecision.execute();
            } else {
                decision = ExecutionDecision.dontExecute("Test is supposed to be executed manually.");
            }

            this.executionDecision.set(decision);

            holder.get().put(testSpec);
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

}
