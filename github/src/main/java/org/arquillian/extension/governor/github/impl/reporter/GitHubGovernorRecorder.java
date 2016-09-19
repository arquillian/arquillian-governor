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

package org.arquillian.extension.governor.github.impl.reporter;

import org.arquillian.extension.governor.api.detector.Detectable;
import org.arquillian.extension.governor.github.api.GitHub;
import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfiguration;
import org.arquillian.recorder.reporter.event.PropertyReportEvent;
import org.arquillian.recorder.reporter.model.entry.KeyValueEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableCellEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableRowEntry;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import java.lang.reflect.Method;
import java.util.TreeSet;


/**
 * @author <a href="mailto:dpawar@redhat.com">Dipak Pawar</a>
 */

public class GitHubGovernorRecorder {

    @Inject
    private Instance<GitHubGovernorConfiguration> gitHubGovernorConfigurationInstance;

    @Inject
    private Event<PropertyReportEvent> propertyReportEvent;

    public void setGitHubGovernorConfigurationInstance(Instance<GitHubGovernorConfiguration> gitHubGovernorConfigurationInstance) {
        this.gitHubGovernorConfigurationInstance = gitHubGovernorConfigurationInstance;
    }

    public void setPropertyReportEvent(Event<PropertyReportEvent> propertyReportEvent) {
        this.propertyReportEvent = propertyReportEvent;
    }

    public void gitHubReportEntries(@Observes After event) {
        final Method testMethod = event.getTestMethod();
        final TestClass testClass = event.getTestClass();

        final GitHub gitHubValue = getGitHubValue(testMethod, testClass);

        if (gitHubValue != null) {
            final GitHubGovernorConfiguration configuration = gitHubGovernorConfigurationInstance.get();

            final String gitHubIssueURL = contructGitHubIssueURL(configuration, gitHubValue);

            final Class<? extends Detectable>[] detectables = gitHubValue.detector().value();
            final TreeSet<String> uniqDetectables = new TreeSet<String>();

            for (final Class<? extends Detectable> detectable : detectables) {
                uniqDetectables.add(detectable.getSimpleName());
            }

            String detectablesName = "";
            for (final String detectable : uniqDetectables) {
                detectablesName += (detectablesName == "" ? "" : ",") + detectable;
            }

            final TableEntry gitHubDetector = new TableEntry();
            gitHubDetector.setTableName("GitHubOptions");
            gitHubDetector.getTableHead().getRow().addCells(new TableCellEntry("Force"), new TableCellEntry("Detector Value"), new TableCellEntry("Detector Strategy"));

            final TableRowEntry row = new TableRowEntry();
            row.addCells(new TableCellEntry(String.valueOf(gitHubValue.force())), new TableCellEntry(String.valueOf(gitHubValue.detector().strategy().getSimpleName())), new TableCellEntry(detectablesName));
            gitHubDetector.getTableBody().addRow(row);

            propertyReportEvent.fire(new PropertyReportEvent(new KeyValueEntry("GitHub URL", gitHubIssueURL)));
            propertyReportEvent.fire(new PropertyReportEvent(gitHubDetector));
        }
    }

    private String contructGitHubIssueURL(GitHubGovernorConfiguration configuration, GitHub gitHub) {
        String url = configuration.getDefaultGithubURL();
        if (configuration.getRepositoryUser() != "") {
            url += ("/" + configuration.getRepositoryUser());
        }
        if (configuration.getRepository() != "") {
            url +=  ("/" + configuration.getRepository());
        }
        if (gitHub.value() != "") {
            url += ("/issues/" + gitHub.value());
        }
        return url;
    }

    private GitHub getGitHubValue(Method method, TestClass testClass) {
        GitHub gitHub =  method.getAnnotation(GitHub.class);
        if (gitHub == null) {
            gitHub = testClass.getAnnotation(GitHub.class);
        }
        return gitHub;
    }
}
