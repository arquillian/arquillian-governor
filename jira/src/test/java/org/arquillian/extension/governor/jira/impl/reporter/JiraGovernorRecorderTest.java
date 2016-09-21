package org.arquillian.extension.governor.jira.impl.reporter;

import org.arquillian.extension.governor.api.detector.Detector;
import org.arquillian.extension.governor.api.detector.utils.OS;
import org.arquillian.extension.governor.jira.api.Jira;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
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
public class JiraGovernorRecorderTest {

    private final static String JIRA_SERVER = "https://jboss.issues.org";

    @Mock
    private Event<PropertyReportEvent> propertyReportEvent;

    @Mock
    private Instance<JiraGovernorConfiguration> jiraGovernorConfigurationInstance;

    @Captor
    ArgumentCaptor<PropertyReportEvent> propertyReportEventArgumentCaptor;

    private JiraGovernorRecorder jiraGovernorRecorder;

    private JiraGovernorConfiguration jiraGovernorConfiguration;

    @Before
    public void setup() {
        jiraGovernorConfiguration = new JiraGovernorConfiguration();
        jiraGovernorConfiguration.setServer(JIRA_SERVER);
        jiraGovernorRecorder = new JiraGovernorRecorder();
        jiraGovernorRecorder.setPropertyReportEvent(propertyReportEvent);
        jiraGovernorRecorder.setJiraGovernorConfigurationInstance(jiraGovernorConfigurationInstance);
        when(jiraGovernorConfigurationInstance.get()).thenReturn(jiraGovernorConfiguration);
    }

    @Test
    public void shouldReportJiraGovernorParamsForMethod() {

        After after = new After(new FakeTestClass(), getMethod("dummyTest"));
        jiraGovernorRecorder.jiraReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List <PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();

        KeyValueEntry keyValueEntry = new KeyValueEntry("JIRA URL","https://jboss.issues.org/browse/ARQ-123");

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force", "true");
        tableOptions.put("Detector Value", "And");
        tableOptions.put("Detector Strategy", "Unix");

        assertThat(propertyReportEvents).hasSize(2).extracting("propertyEntry.class").contains(KeyValueEntry.class, TableEntry.class);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", KeyValueEntry.class).contains(keyValueEntry);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", TableEntry.class) .contains(getTableEntry("JiraOptions", tableOptions));
    }

    @Test
    public void sholdReportJiraGovernorParamsForClass(){
        After after = new After(new FakeTestClass(), getMethod("someTestMethod"));
        jiraGovernorRecorder.jiraReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List <PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();

        KeyValueEntry jiraURL = new KeyValueEntry("JIRA URL","https://jboss.issues.org/browse/ARQ-234");

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force", "false");
        tableOptions.put("Detector Value", "And");
        tableOptions.put("Detector Strategy", "Windows");

        assertThat(propertyReportEvents).hasSize(2).extracting("propertyEntry.class").contains(KeyValueEntry.class, TableEntry.class);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", KeyValueEntry.class).contains(jiraURL);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", TableEntry.class) .contains(getTableEntry("JiraOptions", tableOptions));
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

    @Jira(value = "ARQ-234",  detector = @Detector(value = OS.Windows.class))
    private static final class FakeTestClass {
        @Test
        @Jira(value = "ARQ-123", detector = @Detector(value = OS.Unix.class), force = true)
        public void dummyTest() {
        }

        @Test
        public void someTestMethod() {
        }
    }
}
