package org.arquillian.extension.governor.api;

import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;

public abstract class GovernorConfigurationParser<T extends Configuration>
{
    private static final String DEFAULT_EXTENSION_SUFFIX = "default";

    private final String extensionName;

    public GovernorConfigurationParser(String extensionName)
    {
        this.extensionName = extensionName;
    }

    public abstract T prepareConfiguration(ExtensionDef extension) throws GovernorConfigurationException;

    public Map<String, T> parse(ArquillianDescriptor arquillianDescriptor) throws GovernorConfigurationException
    {
        Map<String, T> configurations = new HashMap<String, T>();

        for (final ExtensionDef extension : arquillianDescriptor.getExtensions())
        {
            if (extension.getExtensionName().startsWith(extensionName))
            {
                configurations.put(parseConfigurationName(extension.getExtensionName()), prepareConfiguration(extension));
            }
        }

        return configurations;
    }

    private String parseConfigurationName(String extensionName) throws GovernorConfigurationException
    {
        if (extensionName.equals(this.extensionName))
        {
            return DEFAULT_EXTENSION_SUFFIX;
        }

        // governor-extension-name -> +2 after dash to get at least one letter of suffix
        if (extensionName.length() < this.extensionName.length() + 2)
        {
            throw new GovernorConfigurationException(String.format("Your Governor configuration '%s' does not have any suffix.", extensionName));
        }

        return extensionName.substring(this.extensionName.length() + 1);
    }
}
