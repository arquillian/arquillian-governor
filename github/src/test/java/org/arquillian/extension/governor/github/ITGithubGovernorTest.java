package org.arquillian.extension.governor.github;

import org.arquillian.extension.governor.github.api.Github;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class ITGithubGovernorTest {

    @ArquillianResource
    GitHubClient githubClient;

    @Test
    @Github(value = "1", force = true)
    public void closeIssue() {
        assertThat(githubClient, notNullValue());
    }
}
