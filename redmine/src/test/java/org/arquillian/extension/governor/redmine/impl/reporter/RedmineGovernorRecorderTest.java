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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedmineGovernorRecorderTest {

    private static final String SERVER = "http://redmine.test";

    @Mock
    private Event<PropertyReportEvent> propertyReportEvent;

    private RedmineGovernorConfiguration redmineGovernorConfiguration;

    @Mock
    private Instance<RedmineGovernorConfiguration> redmineGovernorConfigurationInstance;

    @Captor
    ArgumentCaptor<PropertyReportEvent> propertyReportEventArgumentCaptor;

    private RedmineGovernorRecorder redmineGovernorRecorder;

    @Before
    public void setup() {
        redmineGovernorRecorder = new RedmineGovernorRecorder();
        redmineGovernorRecorder.setPropertyReportEvent(propertyReportEvent);
        redmineGovernorRecorder.setRedmineGovernorConfigurationInstance(redmineGovernorConfigurationInstance);
        redmineGovernorConfiguration = new RedmineGovernorConfiguration();
        redmineGovernorConfiguration.setServer(SERVER);
        when(redmineGovernorConfigurationInstance.get()).thenReturn(redmineGovernorConfiguration);
    }

    @Test
    public void shouldReportRedmineGovernorParamsForMethod() {

        After after = new After(new FakeTestClass(), getMethod("dummyTest"));
        redmineGovernorRecorder.redmineReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List<PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();

        KeyValueEntry redmineURL = new KeyValueEntry("Redmine URL","http://redmine.test/issues/2");

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force","false");

        assertThat(propertyReportEvents).hasSize(2).extracting("propertyEntry.class").contains(KeyValueEntry.class, TableEntry.class);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", KeyValueEntry.class).contains(redmineURL);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", TableEntry.class) .contains(getTableEntry("RedmineOptions", tableOptions));
    }

    @Test
    public void sholdReportRedmineGovernorParamsForClass(){
        After after = new After(new FakeTestClass(), getMethod("someTestMethod"));
        redmineGovernorRecorder.redmineReportEntries(after);

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List <PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();

        KeyValueEntry redmineURL = new KeyValueEntry("Redmine URL","http://redmine.test/issues/1");

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force","true");

        assertThat(propertyReportEvents).hasSize(2).extracting("propertyEntry.class").contains(KeyValueEntry.class, TableEntry.class);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", KeyValueEntry.class).contains(redmineURL);
        assertThat(propertyReportEvents).extracting("propertyEntry").filteredOn("class", TableEntry.class) .contains(getTableEntry("RedmineOptions", tableOptions));

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

    @Redmine(value = "1",  force = true)
    private static final class FakeTestClass {
        @Test
        @Redmine(value = "2")
        public void dummyTest() {
        }

        @Test
        public void someTestMethod() {
        }
    }
}
