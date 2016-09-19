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

import org.arquillian.extension.governor.github.api.GitHub;
import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfiguration;
import org.arquillian.recorder.reporter.PropertyEntry;
import org.arquillian.recorder.reporter.event.PropertyReportEvent;
import org.arquillian.recorder.reporter.model.entry.KeyValueEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableCellEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableRowEntry;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GitHubGovernorRecorderTest {
    private static final String DEFAULT_REPOSITORY = "governor-recorder-test";
    private static final String DEFAULT_REPOSITORY_USERNAME = "dipak-pawar";

    @Mock
    private Event<PropertyReportEvent> propertyReportEvent;

    private GitHubGovernorConfiguration gitHubGovernorConfiguration;

    @Mock
    private Instance<GitHubGovernorConfiguration> gitHubGovernorConfigurationInstance;

    @Captor
    ArgumentCaptor<PropertyReportEvent> propertyReportEventArgumentCaptor;

    private GitHubGovernorRecorder gitHubGovernorRecorder;

    @Before
    public void setup() {
        gitHubGovernorRecorder= new GitHubGovernorRecorder();
        gitHubGovernorRecorder.setPropertyReportEvent(propertyReportEvent);
        gitHubGovernorRecorder.setGitHubGovernorConfigurationInstance(gitHubGovernorConfigurationInstance);

        gitHubGovernorConfiguration = new GitHubGovernorConfiguration();
        gitHubGovernorConfiguration.setRepository(DEFAULT_REPOSITORY);
        gitHubGovernorConfiguration.setRepositoryUser(DEFAULT_REPOSITORY_USERNAME);
        when(gitHubGovernorConfigurationInstance.get()).thenReturn(gitHubGovernorConfiguration);
    }

    @Test
    public void shouldReportGitHubGovernorParamsForMethod() throws NoSuchMethodException {

        After after = new After(new FakeTestClass(), FakeTestClass.class.getMethod("dummyTest"));
        gitHubGovernorRecorder.gitHubReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());
        List<PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();
        assertEquals(propertyReportEvents.size(), 2);
        PropertyEntry keyValueEntry = propertyReportEvents.get(0).getPropertyEntry();

        assertThat(keyValueEntry, instanceOf(KeyValueEntry.class));
        KeyValueEntry gitHubURL = (KeyValueEntry) keyValueEntry;
        assertTrue(gitHubURL.equals(new KeyValueEntry("GitHub URL","https://github.com/dipak-pawar/governor-recorder-test/issues/2")));

        PropertyEntry tableEntry = propertyReportEvents.get(1).getPropertyEntry();
        assertThat(tableEntry, instanceOf(TableEntry.class));
        TableEntry detectors = (TableEntry) tableEntry;

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force", "false");
        tableOptions.put("Detector Value", "And");
        tableOptions.put("Detector Strategy", "True");

        assertTrue(detectors.equals(getTableEntry("GitHubOptions", tableOptions)));
    }

    @Test
    public void sholdReportGitHubGovernorParamsForClass(){
        try {
            After after = new After(new FakeTestClass(), FakeTestClass.class.getMethod("someTestMethod"));
            gitHubGovernorRecorder.gitHubReportEntries(after);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List<PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();
        assertEquals(propertyReportEvents.size(), 2);
        PropertyEntry keyValueEntry = propertyReportEvents.get(0).getPropertyEntry();

        assertThat(keyValueEntry, instanceOf(KeyValueEntry.class));
        KeyValueEntry gitHubURL = (KeyValueEntry) keyValueEntry;
        assertTrue(gitHubURL.equals(new KeyValueEntry("GitHub URL","https://github.com/dipak-pawar/governor-recorder-test/issues/1")));

        PropertyEntry tableEntry = propertyReportEvents.get(1).getPropertyEntry();
        assertThat(tableEntry, instanceOf(TableEntry.class));
        TableEntry detectors = (TableEntry) tableEntry;

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force", "true");
        tableOptions.put("Detector Value", "And");
        tableOptions.put("Detector Strategy", "True");

        assertTrue(detectors.equals(getTableEntry("GitHubOptions", tableOptions)));
    }

    private TableEntry getTableEntry(String tableName, LinkedHashMap<String, String> tableOptions){
        final TableEntry detector = new TableEntry();
        detector.setTableName(tableName);
        for (String tableHead: tableOptions.keySet()) {
            detector.getTableHead().getRow().addCell(new TableCellEntry(tableHead));
        }
        TableRowEntry row = contructRow(tableOptions.values());
        detector.getTableBody().addRow(row);

        return detector;
    }

    private TableRowEntry contructRow(Collection<String> cellContents){
        final TableRowEntry row = new TableRowEntry();
        for (String cellContent: cellContents) {
            row.addCell(new TableCellEntry(cellContent));
        }
        return row;
    }

    @GitHub(value = "1",  force = true)
    private static final class FakeTestClass {
        @Test
        @GitHub(value = "2")
        public void dummyTest() {
        }

        @Test
        public void someTestMethod() {
        }
    }
}
