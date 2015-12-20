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
package org.arquillian.extension.governor.redmine.impl;

import org.arquillian.extension.governor.api.GovernorClientFactory;
import org.arquillian.extension.governor.redmine.configuration.RedmineGovernorConfiguration;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 *
 */
public class RedmineGovernorClientFactory implements GovernorClientFactory<RedmineGovernorConfiguration, RedmineGovernorClient>
{

    private RedmineGovernorConfiguration redmineGovernorConfiguration;

    @Override
    public RedmineGovernorClient build(RedmineGovernorConfiguration governorConfiguration) throws Exception
    {
        Validate.notNull(governorConfiguration, "Redmine governor configuration has to be set.");
        this.redmineGovernorConfiguration = governorConfiguration;

        RedmineGovernorClient redmineGovernorClient = new RedmineGovernorClient(redmineGovernorConfiguration);
        redmineGovernorClient.setGovernorStrategy(new RedmineGovernorStrategy(this.redmineGovernorConfiguration));

        return redmineGovernorClient;
    }

}
