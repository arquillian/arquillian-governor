package org.arquillian.extension.governor.jira.impl.reporter;


import org.arquillian.extension.governor.api.GovernorConfigurationException;
import org.arquillian.extension.governor.api.GovernorRegistry;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;


/**
 * @author <a href="mailto:dpawar@redhat.com">Dipak Pawar</a>
 */
public class JiraGovernorRecorder {

    @Inject
    private Instance<JiraGovernorConfiguration> jira;

    @Inject
    Event<PropertyReportEvent> propertyReportEvent;

    public void jiraReportEntries(@Observes After event, GovernorRegistry governorRegistry) {

        Method testMethod = event.getTestMethod();
        TestClass testClass = event.getTestClass();
        String jiraServer = jira.get().getServer();

        if (testMethod.isAnnotationPresent(Jira.class) || testClass.isAnnotationPresent(Jira.class)) {
            Jira jiraValue = testMethod.getAnnotation(Jira.class);
            for (final Map.Entry<Method, List<Annotation>> entry : governorRegistry.get().entrySet()) {
                if (entry.getKey().toString().equals(testMethod.toString())) {
                    for (Annotation annotation : entry.getValue()) {
                        if (annotation.annotationType() == Jira.class) {
                            jiraValue = ((Jira) annotation);
                         }
                    }
                }
            }

            if (jiraValue != null) {
                String issueURL = constructJiraURL(jiraServer,jiraValue.value());

                Class<? extends Detectable>[] detectables = jiraValue.detector().value();
                TreeSet<String> uniqDetectables = new TreeSet<String>();

                for (Class<? extends Detectable> detectable : detectables) {
                    uniqDetectables.add(detectable.getSimpleName());
                }

                String detectablesName = "";
                for (String detectable : uniqDetectables
                        ) {
                    detectablesName += (detectablesName == "" ? "" : ",") + detectable;
                }

                TableEntry jiraDetector = new TableEntry();
                jiraDetector.setTableName("JiraOptions");
                jiraDetector.getTableHead().getRow().addCells(new TableCellEntry("force"), new TableCellEntry("Detector Value"), new TableCellEntry("Detector Strategy"));

                TableRowEntry row = new TableRowEntry();

                row.addCells(new TableCellEntry(String.valueOf(jiraValue.force())),
                        new TableCellEntry(String.valueOf(jiraValue.detector().strategy().getSimpleName())),
                        new TableCellEntry(detectablesName)
                );
                jiraDetector.getTableBody().addRow(row);

                propertyReportEvent.fire(new PropertyReportEvent(new KeyValueEntry("JIRA URL", issueURL)));
                propertyReportEvent.fire(new PropertyReportEvent(jiraDetector));
            }
        }
    }

    public String constructJiraURL(String server, String issueId){
        if (!server.endsWith("/")){
            server += "/";
        }
        String issueURL = server + "browse/" + issueId;
        return issueURL;
    }
}
