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
package org.arquillian.extension.skipper.test;

import org.arquillian.extension.governor.skipper.api.Status;
import org.arquillian.extension.governor.skipper.api.TestSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SkipperTestClass_2 extends AbstractArquillianClass {

    @Test
    @TestSpec(
        author = "Stefan Miklosovic",
        assertion = "this test should pass",
        feature = "tests if true returns true",
        issue = "ARQ-6",
        prerequisites = "have java",
        status = Status.AUTOMATED,
        steps = "some steps",
        test = "tests stuff"
        )
    public void someTest() {
        Assert.assertTrue(true);
    }

    @Test
    @TestSpec(
        author = "Stefan Miklosovic",
        assertion = "this test should pass",
        feature = "tests if true returns true tests if true returns true "
            + "tests if true returns true tests if true returns true "
            + "tests if true returns true tests if true returns true "
            + "tests if true returns true tests if true returns true",
        issue = "ARQ-5",
        prerequisites = "have java",
        status = Status.MANUAL,
        steps = "some steps",
        test = "tests stuff"
        )
    public void someTest2() {
        Assert.assertTrue(true);
    }
}
