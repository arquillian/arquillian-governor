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
package org.arquillian.extension.governor.github;

import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfigurator;
import org.arquillian.extension.governor.github.enricher.GitHubClientProvider;
import org.arquillian.extension.governor.github.impl.GitHubTestExecutionDecider;
import org.arquillian.extension.governor.github.impl.reporter.GitHubGovernorRecorder;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 */
public class GitHubGovernorExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(GitHubTestExecutionDecider.class);
        builder.observer(GitHubGovernorConfigurator.class);

        builder.service(TestExecutionDecider.class, GitHubTestExecutionDecider.class);
        builder.service(ResourceProvider.class, GitHubClientProvider.class);

        //Only if recorder-reporter is in classpath we should provide reporting capabilities.
        if (Validate.classExists("org.arquillian.recorder.reporter.ReporterExtension")) {
            builder.observer(GitHubGovernorRecorder.class);
        }
    }

}
