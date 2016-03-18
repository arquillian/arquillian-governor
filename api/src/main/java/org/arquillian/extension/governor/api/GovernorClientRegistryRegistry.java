package org.arquillian.extension.governor.api;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class GovernorClientRegistryRegistry
{
    private static GovernorClientRegistryRegistry instance;

    private GovernorClientRegistryRegistry()
    {
    }

    public static GovernorClientRegistryRegistry instance()
    {
        if (instance == null)
        {
            instance = new GovernorClientRegistryRegistry();
        }

        return instance;
    }

    private final Map<Class<? extends Annotation>, GovernorClientRegistry> registry = new HashMap<Class<? extends Annotation>, GovernorClientRegistry>();

    public void add(Class<? extends Annotation> governor, GovernorClientRegistry registry)
    {
        if (!governor.isAnnotationPresent(Governor.class))
        {
            throw new IllegalArgumentException("Governor annotation is not present.");
        }

        this.registry.put(governor, registry);
    }

    public GovernorClientRegistry get(Class<? extends Annotation> governor)
    {
        return registry.get(governor);
    }
}
