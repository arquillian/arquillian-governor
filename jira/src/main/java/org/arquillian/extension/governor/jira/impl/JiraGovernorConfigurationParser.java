package org.arquillian.extension.governor.jira.impl;

import org.arquillian.extension.governor.api.GovernorConfigurationException;
import org.arquillian.extension.governor.api.GovernorConfigurationParser;
import org.arquillian.extension.governor.jira.configuration.JiraGovernorConfiguration;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;

public class JiraGovernorConfigurationParser extends GovernorConfigurationParser<JiraGovernorConfiguration>
{
    public JiraGovernorConfigurationParser(String extensionName)
    {
        super(extensionName);
    }

    @Override
    public JiraGovernorConfiguration prepareConfiguration(ExtensionDef extension) throws GovernorConfigurationException
    {
        JiraGovernorConfiguration configuration = new JiraGovernorConfiguration();
        configuration.setConfiguration(extension.getExtensionProperties());
        configuration.validate();

        return configuration;
    }

}
