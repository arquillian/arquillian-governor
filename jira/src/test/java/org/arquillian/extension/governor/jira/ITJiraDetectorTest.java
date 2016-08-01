/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.governor.jira;


import org.arquillian.extension.governor.api.detector.Detectable;
import org.arquillian.extension.governor.api.detector.Detector;
import org.arquillian.extension.governor.api.detector.Strategy;
import org.arquillian.extension.governor.jira.api.Jira;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@RunAsClient
public class ITJiraDetectorTest {
    private static final String UNRESOLVED_ISSUE = "ARQ-831";
    private static final String RESOLVED_ISSUE = "ARQ-2005";

    private static final int EXPECTED_TESTS_COUNT = 5;
    private static volatile int TESTS_COUNT = 0;

    @After
    public void afterTest() {
        TESTS_COUNT++;
    }

    @AfterClass
    public static void verify() {
        Assert.assertEquals("Not all expected tests were executed", EXPECTED_TESTS_COUNT, TESTS_COUNT);
    }

    // Execute
    @Test
    @Jira(value = RESOLVED_ISSUE, detector = @Detector(Detectable.False.class))
    public void resolvedIssueDetectorFailedTest() {
        Assert.assertTrue(true);
    }

    // Execute
    @Test
    @Jira(value = RESOLVED_ISSUE, detector = @Detector(Detectable.True.class))
    public void resolvedIssueDetectorPassedTest() {
        Assert.assertTrue(true);
    }

    // Execute
    @Test
    @Jira(value = UNRESOLVED_ISSUE, detector = @Detector(Detectable.False.class))
    public void unresolvedIssueDetectorFailedTest() {
        Assert.assertTrue(true);
    }

    // Skip
    @Test
    @Jira(value = UNRESOLVED_ISSUE, detector = @Detector(Detectable.True.class))
    public void unresolvedIssueDetectorPassedTest() {
        Assert.assertTrue(false);
    }

    // Execute
    @Test
    @Jira(value = RESOLVED_ISSUE,
            detector = @Detector({ Detectable.True.class, Detectable.False.class })
    )
    public void resolvedIssueDetectorsFailedTest() {
        Assert.assertTrue(true);
    }

    // Execute
    @Test
    @Jira(value = UNRESOLVED_ISSUE,
            detector = @Detector({ Detectable.True.class, Detectable.False.class })
    )
    public void unresolvedIssueDetectorsFailedTest() {
        Assert.assertTrue(true);
    }

    // Skip
    @Test
    @Jira(value = UNRESOLVED_ISSUE,
            detector = @Detector({ Detectable.True.class, Detectable.True.class })
    )
    public void unresolvedIssueDetectorsPassedTest() {
        Assert.assertTrue(false);
    }

    // Skip
    @Test
    @Jira(value = UNRESOLVED_ISSUE,
            detector = @Detector(value = { Detectable.True.class, Detectable.False.class}, strategy = Strategy.Or.class)
    )
    public void unresolvedIssueDetectorsFailedStrategyOrTest() {
        Assert.assertTrue(true);
    }
}
