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

package org.arquillian.extension.governor.jira.impl.reporter;

import org.arquillian.extension.governor.api.detector.Detectable;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
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
import java.util.Set;
import java.util.TreeSet;


/**
 * @author <a href="mailto:dpawar@redhat.com">Dipak Pawar</a>
 */
public class JiraGovernorRecorder {

    @Inject
    private Instance<JiraGovernorConfiguration> jiraGovernorConfigurationInstance;

    public void setJiraGovernorConfigurationInstance(Instance<JiraGovernorConfiguration> jiraGovernorConfigurationInstance) {
        this.jiraGovernorConfigurationInstance = jiraGovernorConfigurationInstance;
    }

    @Inject
    private Event<PropertyReportEvent> propertyReportEvent;

    public void setPropertyReportEvent(Event<PropertyReportEvent> propertyReportEvent) {
        this.propertyReportEvent = propertyReportEvent;
    }

    public void jiraReportEntries(@Observes After event) {
        final Method method = event.getTestMethod();

        final TestClass testClass = event.getTestClass();
        final String jiraServer = jiraGovernorConfigurationInstance.get().getServer();

        final Jira jira = getJiraValue(method, testClass);
        if (jira != null) {
            final String issueURL = constructJiraIssueURL(jiraServer, jira.value());

            final Class<? extends Detectable>[] detectables = jira.detector().value();
            final Set<String> uniqDetectables = new TreeSet<String>();

            for (final Class<? extends Detectable> detectable : detectables) {
                uniqDetectables.add(detectable.getSimpleName());
            }

            String detectablesName = "";
            for (final String detectable : uniqDetectables) {
                detectablesName += (detectablesName == "" ? "" : ",") + detectable;
            }

            final TableEntry jiraDetector = new TableEntry();
            jiraDetector.setTableName("JiraOptions");
            jiraDetector.getTableHead().getRow().addCells(new TableCellEntry("Force"), new TableCellEntry("Detector Value"), new TableCellEntry("Detector Strategy"));

            final TableRowEntry row = new TableRowEntry();

            row.addCells(new TableCellEntry(String.valueOf(jira.force())),
                    new TableCellEntry(String.valueOf(jira.detector().strategy().getSimpleName())),
                    new TableCellEntry(detectablesName)
            );
            jiraDetector.getTableBody().addRow(row);

            propertyReportEvent.fire(new PropertyReportEvent(new KeyValueEntry("JIRA URL", issueURL)));
            propertyReportEvent.fire(new PropertyReportEvent(jiraDetector));
        }
    }

    private String constructJiraIssueURL(String server, String issueId) {
        if (!server.endsWith("/")) {
            server += "/";
        }
        final String issueURL = server + "browse/" + issueId;
        return issueURL;
    }

    private Jira getJiraValue(Method method, TestClass testClass) {
        Jira jira =  method.getAnnotation(Jira.class);
        if (jira == null) {
            jira = testClass.getAnnotation(Jira.class);
        }
        return jira;
    }
}
