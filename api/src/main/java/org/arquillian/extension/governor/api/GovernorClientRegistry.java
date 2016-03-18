package org.arquillian.extension.governor.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GovernorClientRegistry
{

    private final Map<String, GovernorClient<?, ?>> register = new HashMap<String, GovernorClient<?, ?>>();

    public void add(String name, GovernorClient<?, ?> governorClient)
    {
        register.put(name, governorClient);
    }

    public GovernorClient<?, ?> get(String name)
    {
        return register.get(name);
    }

    public Map<String, GovernorClient<?, ?>> getAll()
    {
        return Collections.unmodifiableMap(register);
    }
}
