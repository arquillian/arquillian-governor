package org.arquillian.extension.governor.redmine.impl.reporter;

import org.arquillian.extension.governor.redmine.api.Redmine;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
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
import java.util.TreeMap;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by dipak on 9/19/16.
 */
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

        try {
            After after = new After(new FakeTestClass(), FakeTestClass.class.getMethod("dummyTest"));
            redmineGovernorRecorder.redmineReportEntries(after);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List<PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();
        assertEquals(propertyReportEvents.size(), 2);

        PropertyEntry keyValueEntry = propertyReportEvents.get(0).getPropertyEntry();
        assertThat(keyValueEntry, instanceOf(KeyValueEntry.class));
        KeyValueEntry redmineURL = (KeyValueEntry) keyValueEntry;
        assertTrue(redmineURL.equals(new KeyValueEntry("Redmine URL","http://redmine.test/issues/2")));

        PropertyEntry tableEntry = propertyReportEvents.get(1).getPropertyEntry();
        assertThat(tableEntry, instanceOf(TableEntry.class));
        TableEntry detectors = (TableEntry) tableEntry;

        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force","false");

        assertTrue(detectors.equals(getTableEntry("RedmineOptions", tableOptions)));
    }

    @Test
    public void sholdReportRedmineGovernorParamsForClass(){
        try {
            After after = new After(new FakeTestClass(), FakeTestClass.class.getMethod("someTestMethod"));
            redmineGovernorRecorder.redmineReportEntries(after);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        verify(propertyReportEvent, times(2)).fire(propertyReportEventArgumentCaptor.capture());

        List <PropertyReportEvent> propertyReportEvents = propertyReportEventArgumentCaptor.getAllValues();
        assertEquals(propertyReportEvents.size(), 2);
        PropertyEntry keyValueEntry = propertyReportEvents.get(0).getPropertyEntry();

        assertThat(keyValueEntry, instanceOf(KeyValueEntry.class));
        KeyValueEntry redmineURL = (KeyValueEntry) keyValueEntry;
        assertTrue(redmineURL.equals(new KeyValueEntry("Redmine URL","http://redmine.test/issues/1")));

        PropertyEntry tableEntry = propertyReportEvents.get(1).getPropertyEntry();
        assertThat(tableEntry, instanceOf(TableEntry.class));
        TableEntry detectors = (TableEntry) tableEntry;
        LinkedHashMap<String, String> tableOptions = new LinkedHashMap<String, String>();
        tableOptions.put("Force","true");
        assertTrue(detectors.equals(getTableEntry("RedmineOptions",tableOptions)));
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
