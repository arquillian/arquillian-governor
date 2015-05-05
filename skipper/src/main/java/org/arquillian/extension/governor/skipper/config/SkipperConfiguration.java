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
package org.arquillian.extension.governor.skipper.config;

import org.arquillian.extension.governor.api.Configuration;
import org.arquillian.extension.governor.api.GovernorConfigurationException;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SkipperConfiguration extends Configuration {

    private String plainAdoc = null;

    /**
     * When set, there will be simple adoc table with the report.
     * @param plainAdoc
     */
    public void setPlainAdoc(String plainAdoc) {
        setProperty("plainAdoc", plainAdoc);
    }

    public String getPlainAdoc() {
        return getProperty("plainAdoc", "");
    }

    @Override
    public void validate() throws GovernorConfigurationException {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-40s %s\n", "plainAdoc", getPlainAdoc()));

        return sb.toString();
    }
}
