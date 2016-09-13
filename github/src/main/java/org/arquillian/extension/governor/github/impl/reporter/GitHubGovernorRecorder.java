package org.arquillian.extension.governor.github.impl.reporter;

import org.arquillian.extension.governor.api.detector.Detectable;
import org.arquillian.extension.governor.github.api.GitHub;
import org.arquillian.extension.governor.github.configuration.GitHubGovernorConfiguration;
import org.arquillian.recorder.reporter.event.PropertyReportEvent;
import org.arquillian.recorder.reporter.model.entry.KeyValueEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableCellEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableRowEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import java.lang.reflect.Method;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:dpawar@redhat.com">Dipak Pawar</a>
 */
public class GitHubGovernorRecorder {

    @Inject
    private Instance<GitHubGovernorConfiguration> gitHub;

    @Inject
    Event<PropertyReportEvent> propertyReportEvent;

    public void gitHubReportEntries(@Observes After event) {

        Method testMethod = event.getTestMethod();
        TestClass testClass = event.getTestClass();

        GitHub gitHubValue = getJira(testMethod, testClass);
        if(gitHubValue != null){
            GitHubGovernorConfiguration configuration = gitHub.get();

            String gitHubIssueURL = contructGitHubIssueURL(configuration,gitHubValue);

            Class<? extends Detectable>[] detectables = gitHubValue.detector().value();
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
            jiraDetector.setTableName("GitHubOptions");
            jiraDetector.getTableHead().getRow().addCells(new TableCellEntry("Force"), new TableCellEntry("Detector Value"), new TableCellEntry("Detector Strategy"));

            TableRowEntry row = new TableRowEntry();

            row.addCells(new TableCellEntry(String.valueOf(gitHubValue.force())),
                    new TableCellEntry(String.valueOf(gitHubValue.detector().strategy().getSimpleName())),
                    new TableCellEntry(detectablesName)
            );
            jiraDetector.getTableBody().addRow(row);

            propertyReportEvent.fire(new PropertyReportEvent(new KeyValueEntry("GitHub URL", gitHubIssueURL)));
            propertyReportEvent.fire(new PropertyReportEvent(jiraDetector));
        }
    }

    public String contructGitHubIssueURL(GitHubGovernorConfiguration configuration, GitHub gitHub){
        String url = configuration.getDefaultGithubURL();
        if (configuration.getRepositoryUser() != "") {
            url += ("/" + configuration.getRepositoryUser());
        }
        if (configuration.getRepository() != ""){
            url +=  ("/" + configuration.getRepository());
        }
        if (gitHub.value() != ""){
            url += ("/issues/" + gitHub.value());
        }
        return url;
    }

    public GitHub getJira(Method method, TestClass testClass){
        GitHub gitHubValue = method.getAnnotation(GitHub.class);
        if (gitHubValue == null){
            gitHubValue = testClass.getAnnotation(GitHub.class);
        }
        return gitHubValue;

    }
}