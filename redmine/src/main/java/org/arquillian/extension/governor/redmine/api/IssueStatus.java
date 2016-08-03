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
package org.arquillian.extension.governor.redmine.api;

/**
 * @author <a href="mailto:rmpestano@gmail.com">Rafael Pestano</a>
 *         <p>
 *         http://demo.redmine.org/issue_statuses.xml
 */
public enum IssueStatus {
    NEW(1),
    IN_PROGRESS(2),
    RESOLVED(3),
    CLOSED(5);

    private final Integer statusCode;

    IssueStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public static boolean isClosed(Integer statusCode) {
        return CLOSED.getStatusCode().equals(statusCode);
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
