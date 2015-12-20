package org.arquillian.extension.governor.redmine;

import com.taskadapter.redmineapi.RedmineManager;
import org.arquillian.extension.governor.redmine.api.Redmine;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class ITRedmineGovernorTest {

    @ArquillianResource
    RedmineManager redmineManager;

    @Ignore("Will pass only if you have redmine setup locally")
    @Test
    @Redmine(value = "1", force = true)
    public void closeIssue() {
        //TODO test using cube and redmine docker image: https://github.com/sameersbn/docker-redmine
        assertThat(redmineManager, notNullValue());
    }

    @Ignore("Will pass only if you have redmine setup locally")
    @Test
    @Redmine(value = "1", openFailed = true)
    public void reOpenIssue() {
        //TODO test using cube and redmine docker image: https://github.com/sameersbn/docker-redmine
        assertThat(redmineManager, nullValue());
    }
}
