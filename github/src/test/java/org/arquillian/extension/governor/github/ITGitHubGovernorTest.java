package org.arquillian.extension.governor.github;

import org.arquillian.extension.governor.github.api.GitHub;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ITGitHubGovernorTest {

    @ArquillianResource
    GitHubClient gitHubClient;

    @Test
    @GitHub(value = "1", force = true)
    public void closeIssue() {
        assertThat(gitHubClient, notNullValue());
    }
}
