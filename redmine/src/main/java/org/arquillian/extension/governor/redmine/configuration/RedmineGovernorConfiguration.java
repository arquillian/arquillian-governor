/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.governor.redmine.configuration;

import org.arquillian.extension.governor.api.Configuration;
import org.arquillian.extension.governor.api.GovernorConfigurationException;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 */
public class RedmineGovernorConfiguration extends Configuration {
    private static final String EMPTY_STRING = "";

    private static final String DEFAULT_REDMINE_CLOSING_MESSAGE = "This Redmine issue was automatically closed by %s with Arquillian Governor Redmine extension.";
    private static final String DEFAULT_REDMINE_OPENING_MESSAGE = "This Redmine issue was automatically opened by %s with Arquillian Governor Redmine extension. Cause: ";

    private String apiKey = resolveApiKey();

    private String server = resolveServer();

    private boolean force = resolveForce();

    private boolean closePassed = resolveClosePassed();

    private boolean openFailed = resolveOpenFailed();

    private String closeOrder = resolveCloseOrder();

    private boolean resolveOpenFailed() {
        return Boolean.valueOf(System.getProperty("redmine.governor.openfailed"));
    }

    public String getApiKey() {
        return resolveSystemProperties(apiKey, "apikey", EMPTY_STRING);
    }

    public void setApiKey(String apiKey) {
        setProperty("apikey", apiKey);
    }

    public String getCloseOrder() {
        return resolveSystemProperties(closeOrder, "closeOrder", EMPTY_STRING);
    }

    public String getServer() {
        return resolveSystemProperties(server, "server", EMPTY_STRING);
    }

    public void setServer(String server) {
        setProperty("server", server);
    }

    public boolean getForce() {
        return Boolean.parseBoolean(resolveSystemProperties(Boolean.toString(force), "force", "false"));
    }

    public void setForce(boolean force) {
        setProperty("force", Boolean.toString(force));
    }

    public boolean getClosePassed() {
        return Boolean.parseBoolean(resolveSystemProperties(Boolean.toString(closePassed), "closePassed", "false"));
    }

    public void setClosePassed(boolean closePassed) {
        setProperty("closePassed", Boolean.toString(closePassed));
    }

    public boolean getOpenFailed() {
        return Boolean.parseBoolean(resolveSystemProperties(Boolean.toString(openFailed), "openFailed", "false"));
    }

    public void setOpenFailed(boolean openFailed) {
        setProperty("openFailed", Boolean.toString(openFailed));
    }

    public String getClosingMessage() {
        return getProperty("closingMessage", DEFAULT_REDMINE_CLOSING_MESSAGE);
    }

    public void setClosingMessage(String closingMessage) {
        setProperty("closingMessage", closingMessage);
    }

    public String getOpeningMessage() {
        return getProperty("openingMessage", DEFAULT_REDMINE_OPENING_MESSAGE);
    }

    public void setOpeningMessage(String closingMessage) {
        setProperty("openingMessage", closingMessage);
    }

    @Override
    public void validate() throws GovernorConfigurationException {
        if (EMPTY_STRING.equals(getServer())) {
            // TODO provide more info on how to set the property
            throw new GovernorConfigurationException("Redmine server is not set.");
        }

        if (EMPTY_STRING.equals(getApiKey())) {
            throw new GovernorConfigurationException("Api key is not set.");
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        // password is not here due to security reasons

        sb.append(String.format("%-40s %s\n", "server", getServer()));
        sb.append(String.format("%-40s %s\n", "apikey", getApiKey()));
        sb.append(String.format("%-40s %s\n", "closeOrder", getCloseOrder()));
        sb.append(String.format("%-40s %s\n", "force", getForce()));
        sb.append(String.format("%-40s %s\n", "closePassed", getClosePassed()));
        sb.append(String.format("%-40s %s\n", "closingMessage", getClosingMessage()));
        sb.append(String.format("%-40s %s\n", "openFailed", getOpenFailed()));
        sb.append(String.format("%-40s %s\n", "openingMessage", getOpeningMessage()));

        return sb.toString();
    }

    // helpers

    private String resolveSystemProperties(String property, String propertName, String defaultValue) {
        if (!property.equals(defaultValue)) {
            return property;
        } else {
            return getProperty(propertName, defaultValue);
        }
    }

    private String resolveApiKey() {
        final String apiKey = System.getProperty("github.governor.apikey");

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return apiKey;
        }

        return EMPTY_STRING;
    }

    private String resolveCloseOrder() {
        final String closeOrder = System.getProperty("github.governor.closeOrder");

        if (closeOrder != null && !closeOrder.trim().isEmpty()) {
            return closeOrder;
        }

        return EMPTY_STRING;
    }

    private String resolveServer() {
        String server = System.getProperty("redmine.governor.server");

        if (server != null && server.length() != 0) {
            if (server.endsWith("/")) {
                server = server.substring(0, server.length() - 1);
            }
            return server;
        }

        return EMPTY_STRING;
    }

    private boolean resolveForce() {
        return Boolean.valueOf(System.getProperty("redmine.governor.force"));
    }

    private boolean resolveClosePassed() {
        return Boolean.valueOf(System.getProperty("redmine.governor.closePassed"));
    }

}
