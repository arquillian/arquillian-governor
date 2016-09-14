package org.arquillian.extension.governor.redmine.impl.reporter;

import org.arquillian.extension.governor.api.GovernorConfigurationException;
import org.arquillian.extension.governor.api.GovernorRegistry;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dpawar@redhat.com">Dipak Pawar</a>
 */

public class RedmineGovernorRecorder {

    @Inject
    private Instance<RedmineGovernorConfiguration> redmine;

    @Inject
    Event<PropertyReportEvent> propertyReportEvent;

    public void redmineReportEntries(@Observes After event, GovernorRegistry governorRegistry) throws GovernorConfigurationException {

        Method testMethod = event.getTestMethod();
        TestClass testClass = event.getTestClass();
        String redmineServerURL = redmine.get().getServer();

        if (testMethod.isAnnotationPresent(Redmine.class) || testClass.isAnnotationPresent(Redmine.class)) {
            Redmine redmineValue = testMethod.getAnnotation(Redmine.class);
            for (final Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().entrySet()) {
                if (entry.getKey().toString().equals(testMethod.toString())) {
                    for (Annotation annotation : entry.getValue()) {
                        if (annotation.annotationType() == Redmine.class) {
                            redmineValue = ((Redmine) annotation);
                        }
                    }
                }
            }

            if (redmineValue != null) {
                String issueURL = constructURL(redmineServerURL,redmineValue.value());

                TableEntry jiraDetector = new TableEntry();
                jiraDetector.setTableName("RedmineOptions");
                jiraDetector.getTableHead().getRow().addCells(new TableCellEntry("force"));

                TableRowEntry row = new TableRowEntry();

                row.addCells(new TableCellEntry(String.valueOf(redmineValue.force())));
                jiraDetector.getTableBody().addRow(row);

                propertyReportEvent.fire(new PropertyReportEvent(new KeyValueEntry("Redmine URL", issueURL)));
                propertyReportEvent.fire(new PropertyReportEvent(jiraDetector));
            }

        }
    }

    public String constructURL(String server, String issueId){
        if (!server.endsWith("/")){
            server += "/";
        }
        String issueURL = server + "issues/" + issueId;
        return issueURL;
    }
}