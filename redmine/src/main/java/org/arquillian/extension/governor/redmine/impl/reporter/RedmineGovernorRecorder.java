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

package org.arquillian.extension.governor.redmine.impl.reporter;

import org.arquillian.extension.governor.redmine.api.Redmine;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
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

/**
 * @author <a href="mailto:dpawar@redhat.com">Dipak Pawar</a>
 */


public class RedmineGovernorRecorder {

    @Inject
    private Instance<RedmineGovernorConfiguration> redmineGovernorConfigurationInstance;

    @Inject
    private Event<PropertyReportEvent> propertyReportEvent;

    public void setRedmineGovernorConfigurationInstance(Instance<RedmineGovernorConfiguration> redmineGovernorConfigurationInstance) {
        this.redmineGovernorConfigurationInstance = redmineGovernorConfigurationInstance;
    }

    public void setPropertyReportEvent(Event<PropertyReportEvent> propertyReportEvent) {
        this.propertyReportEvent = propertyReportEvent;
    }

    public void redmineReportEntries(@Observes After event) {

        final Method testMethod = event.getTestMethod();
        final TestClass testClass = event.getTestClass();
        final String redmineServerURL = redmineGovernorConfigurationInstance.get().getServer();

        final Redmine redmineValue = getRedmineValue(testMethod, testClass);
        if (redmineValue != null) {
            final String issueURL = constructRedmineIssueURL(redmineServerURL, redmineValue.value());

            final TableEntry jiraDetector = new TableEntry();
            jiraDetector.setTableName("RedmineOptions");
            jiraDetector.getTableHead().getRow().addCells(new TableCellEntry("Force"));

            final TableRowEntry row = new TableRowEntry();

            row.addCells(new TableCellEntry(String.valueOf(redmineValue.force())));
            jiraDetector.getTableBody().addRow(row);

            propertyReportEvent.fire(new PropertyReportEvent(new KeyValueEntry("Redmine URL", issueURL)));
            propertyReportEvent.fire(new PropertyReportEvent(jiraDetector));
        }
    }

    private String constructRedmineIssueURL(String server, String issueId) {
        if (!server.endsWith("/")) {
            server += "/";
        }
        final String issueURL = server + "issues/" + issueId;
        return issueURL;
    }


    private Redmine getRedmineValue(Method method, TestClass testClass) {
        Redmine redmine =  method.getAnnotation(Redmine.class);
        if (redmine == null) {
            redmine = testClass.getAnnotation(Redmine.class);
        }
        return redmine;
    }
}
