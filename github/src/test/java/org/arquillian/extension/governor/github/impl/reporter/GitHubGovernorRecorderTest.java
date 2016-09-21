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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;


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
    public void shouldReportGitHubGovernorParamsForMethod() {

        After after = new After(new FakeTestClass(), getMethod("dummyTest"));
        gitHubGovernorRecorder.gitHubReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List<PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();

        KeyValueEntry gitHubURL = new KeyValueEntry("GitHub URL","https://github.com/dipak-pawar/governor-recorder-test/issues/2");

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force", "false");
        tableOptions.put("Detector Value", "And");
        tableOptions.put("Detector Strategy", "True");

        assertThat(propertyReportEvents).hasSize(2).extracting("propertyEntry.class").contains(KeyValueEntry.class, TableEntry.class);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", KeyValueEntry.class).contains(gitHubURL);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", TableEntry.class) .contains(getTableEntry("GitHubOptions", tableOptions));

    }

    @Test
    public void sholdReportGitHubGovernorParamsForClass(){
        After after = new After(new FakeTestClass(), getMethod("someTestMethod"));
        gitHubGovernorRecorder.gitHubReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List<PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();

        KeyValueEntry gitHubURL = new KeyValueEntry("GitHub URL","https://github.com/dipak-pawar/governor-recorder-test/issues/1");

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force", "true");
        tableOptions.put("Detector Value", "And");
        tableOptions.put("Detector Strategy", "True");

        assertThat(propertyReportEvents).hasSize(2).extracting("propertyEntry.class").contains(KeyValueEntry.class, TableEntry.class);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", KeyValueEntry.class).contains(gitHubURL);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", TableEntry.class) .contains(getTableEntry("GitHubOptions", tableOptions));
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

    private Method getMethod(String methodName) {
        Method method = null;
        try {
            method = FakeTestClass.class.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
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
