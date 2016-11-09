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
package org.arquillian.extension.governor.github.configuration;

import org.arquillian.extension.governor.api.Configuration;
import org.arquillian.extension.governor.api.GovernorConfigurationException;

/**
 * @author <a href="mailto:asotobu@gmail.com">Alex Soto</a>
 */
public class GitHubGovernorConfiguration extends Configuration {
    private static final String EMPTY_STRING = "";

    private static final String DEFAULT_GITHUB_CLOSING_MESSAGE = "This GitHub issue was automatically closed by %s with Arquillian Governor GitHub extension.";

    private String defaultGithubURL = "https://github.com";

    private String username = resolveUsername();

    private String password = resolvePassword();

    private String token = resolveToken();

    private String repositoryUser = resolveRepositoryUser();

    private String repository = resolveRepository();

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

    public String getToken() {
        if (!token.equals(EMPTY_STRING)){
            return token;
        }else {
            return getProperty("token", EMPTY_STRING);
        }
    }

    public void setToken(String token) {
        setProperty("token", token);
    }

    public String getRepositoryUser() {
        if (!repositoryUser.equals(EMPTY_STRING)){
            return repositoryUser;
        }else {
            return getProperty("repositoryUser", EMPTY_STRING);
        }
    }

    public void setRepositoryUser(String repositoryUser) {
        setProperty("repositoryUser", repositoryUser);
    }

    public String getRepository() {
        if (!repository.equals(EMPTY_STRING)){
            return repository;
        }else {
            return getProperty("repository", EMPTY_STRING);
        }
    }

    public void setRepository(String repository) {
        setProperty("repository", repository);
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
        if (closePassed == true) {
            return closePassed;
        } else {
            return Boolean.parseBoolean(getProperty("closePassed", Boolean.toString(closePassed)));
        }
    }
    public void setClosePassed(boolean closePassed) {
        setProperty("closePassed", Boolean.toString(closePassed));
    }

    public String getClosingMessage() {
        return getProperty("closingMessage", DEFAULT_GITHUB_CLOSING_MESSAGE);
    }

    public void setClosingMessage(String closingMessage) {
        setProperty("closingMessage", closingMessage);
    }

    public String getDefaultGithubURL() {
        return defaultGithubURL;
    }

    public void setDefaultGithubURL(String defaultGithubURL) {
        this.defaultGithubURL = defaultGithubURL;
    }

    @Override
    public void validate() throws GovernorConfigurationException {
        if (EMPTY_STRING.equals(getRepositoryUser()) || EMPTY_STRING.equals(getRepository())) {
            throw new GovernorConfigurationException("Repository user or repository name are not set - it is an empty String.");
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        // password is not here due to security reasons

        sb.append(String.format("%-40s %s\n", "username", getUsername()));
        sb.append(String.format("%-40s %s\n", "repositoryUser", getRepositoryUser()));
        sb.append(String.format("%-40s %s\n", "repository", getRepository()));
        sb.append(String.format("%-40s %s\n", "force", getForce()));
        sb.append(String.format("%-40s %s\n", "closePassed", getClosePassed()));
        sb.append(String.format("%-40s %s\n", "closingMessage", getClosingMessage()));

        return sb.toString();
    }

    // helpers

    private String resolvePassword() {
        final String password = System.getProperty("github.governor.password");

        if (password != null && password.length() != 0) {
            return password;
        }

        return EMPTY_STRING;
    }

    private String resolveUsername() {
        final String username = System.getProperty("github.governor.username");

        if (username != null && username.length() != 0) {
            return username;
        }

        return EMPTY_STRING;
    }

    private String resolveToken() {
        final String token = System.getProperty("github.governor.token");

        if (token != null && token.length() != 0) {
            return token;
        }

        return EMPTY_STRING;
    }

    private String resolveRepositoryUser() {
        final String repositoryUser = System.getProperty("github.governor.repositoryuser");

        if (repositoryUser != null && repositoryUser.length() != 0) {
            return repositoryUser;
        }

        return EMPTY_STRING;
    }

    private String resolveRepository() {
        final String repository = System.getProperty("github.governor.repository");

        if (repository != null && repository.length() != 0) {
            return repository;
        }

        return EMPTY_STRING;
    }

    private boolean resolveForce() {
        return Boolean.valueOf(System.getProperty("github.governor.force"));
    }

    private boolean resolveClosePassed() {
        return Boolean.valueOf(System.getProperty("github.governor.closepassed"));
    }

}

