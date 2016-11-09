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
package org.arquillian.extension.governor.jira.configuration;

import org.arquillian.extension.governor.api.Configuration;
import org.arquillian.extension.governor.api.GovernorConfigurationException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class JiraGovernorConfiguration extends Configuration {
    private static final String EMPTY_STRING = "";

    private static final String DEFAULT_JIRA_SERVER_ADDRESS = "https://issues.jboss.org";

    private static final String DEFAULT_JIRA_CLOSING_MESSAGE = "This JIRA issue was automatically closed by %s with Arquillian Governor JIRA extension.";

    private String username = resolveUsername();

    private String password = resolvePassword();

    private String server = resolveServer();

    private boolean force = resolveForce();

    private boolean closePassed = resolveClosePassed();

    public String getUsername() {
        if (!username.equals(EMPTY_STRING)){
            return username;
        }else {
            return getProperty("username", EMPTY_STRING);
        }
    }

    public void setUsername(String username) {
        setProperty("username", username);
    }

    public String getPassword() {
        if (!password.equals(EMPTY_STRING)){
            return password;
        }else {
            return getProperty("password", EMPTY_STRING);
        }
    }

    public void setPassword(String password) {
        setProperty("password", password);
    }

    public String getServer() {
        if (!server.equals(DEFAULT_JIRA_SERVER_ADDRESS)){
            return server;
        }else {
            return getProperty("server", DEFAULT_JIRA_SERVER_ADDRESS);
        }
    }

    public void setServer(String server) {
        setProperty("server", server);
    }

    public boolean getForce() {
        if (force == true){
            return force;
        }else {
            return Boolean.parseBoolean(getProperty("force", Boolean.toString(force)));
        }
    }

    public void setForce(boolean force) {
        setProperty("force", Boolean.toString(force));
    }

    public boolean getClosePassed() {
        if (closePassed == true){
            return closePassed;
        }else {
            return Boolean.parseBoolean(getProperty("closePassed", Boolean.toString(closePassed)));
        }
    }

    public void setClosePassed(boolean closePassed) {
        setProperty("closePassed", Boolean.toString(closePassed));
    }

    public String getClosingMessage() {
        return getProperty("closingMessage", DEFAULT_JIRA_CLOSING_MESSAGE);
    }

    public void setClosingMessage(String closingMessage) {
        setProperty("closingMessage", closingMessage);
    }

    public URL getServerURL() throws GovernorConfigurationException {
        URL url = null;

        try {
            url = new URL(getServer());
        } catch (MalformedURLException ex) {
            throw new GovernorConfigurationException("Unable to construct URL of server from address: " + getServer());
        }

        return url;
    }

    public URI getServerURI() throws GovernorConfigurationException {
        URI uri = null;

        try {
            uri = new URI(getServer());
        } catch (URISyntaxException ex) {
            throw new GovernorConfigurationException("Unable to construct URI of server from address: " + getServer());
        }

        return uri;
    }

    @Override
    public void validate() throws GovernorConfigurationException {
        if (getServer().length() == 0) {
            throw new GovernorConfigurationException("Server URL is not set - it is an empty String.");
        }

        getServerURI();
        getServerURL();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        // password is not here due to security reasons

        sb.append(String.format("%-40s %s\n", "username", getUsername()));
        sb.append(String.format("%-40s %s\n", "server", getServer()));
        sb.append(String.format("%-40s %s\n", "force", getForce()));
        sb.append(String.format("%-40s %s\n", "closePassed", getClosePassed()));
        sb.append(String.format("%-40s %s\n", "closingMessage", getClosingMessage()));

        return sb.toString();
    }

    // helpers

    private String resolveServer() {
        final String server = System.getProperty("jira.governor.server");

        if (server != null && server.length() != 0) {
            return server;
        } else {
            return DEFAULT_JIRA_SERVER_ADDRESS;
        }

    }

    private String resolvePassword() {
        final String password = System.getProperty("jira.governor.password");

        if (password != null && password.length() != 0) {
            return password;
        }

        return EMPTY_STRING;
    }

    private String resolveUsername() {
        final String username = System.getProperty("jira.governor.username");

        if (username != null && username.length() != 0) {
            return username;
        }

        return EMPTY_STRING;
    }

    private boolean resolveForce() {
        return Boolean.valueOf(System.getProperty("jira.governor.force"));
    }

    private boolean resolveClosePassed() {
        return Boolean.valueOf(System.getProperty("jira.governor.closepassed"));
    }
}
