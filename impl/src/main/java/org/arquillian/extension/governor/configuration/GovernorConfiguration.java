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
package org.arquillian.extension.governor.configuration;

import org.arquillian.extension.governor.api.Configuration;
import org.arquillian.extension.governor.api.GovernorConfigurationException;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class GovernorConfiguration extends Configuration
{
    private boolean ignore = false;

    private String ignoreOnly = "";

    public void setIgnore(boolean ignore)
    {
        setProperty("ignore", Boolean.toString(ignore));
    }

    public Boolean getIgnore()
    {
        return Boolean.valueOf(getProperty("ignore", Boolean.toString(ignore)));
    }

    public void setIgnoreOnly(String ignoreOnly)
    {
        Validate.notNullOrEmpty(ignoreOnly, "ignoreOnly property can not be a null object nor an empty String.");
        setProperty("ignoreOnly", ignoreOnly);
    }

    public String getIgnoreOnly()
    {
        return getProperty("ignoreOnly", ignoreOnly);
    }

    @Override
    public void validate() throws GovernorConfigurationException
    {
        if (getIgnore())
        {
            if (getIgnoreOnly() != null && getIgnoreOnly().length() != 0)
            {
                throw new GovernorConfigurationException("You have set 'ignore' property to true and you set 'ignoreOnly' as well. "
                    + "Either set 'ignore' and left ignoreOnly unset or left 'ignore' flag unset "
                    + "and set 'ignoreOnly' property.");
            }
        }
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-40s %s\n", "ignore", getIgnore()));
        sb.append(String.format("%-40s %s\n", "ignoreOnly", getIgnoreOnly()));

        return sb.toString();
    }
}
