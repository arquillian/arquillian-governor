package org.arquillian.extension.governor.redmine.api;

/**
 * Created by pestano on 20/12/15.
 *
 * http://demo.redmine.org/issue_statuses.xml
 */
public enum IssueStatus {
    NEW(1), IN_PROGRESS(2), RESOLVED(3), CLOSED(5);


    private final Integer statusCode;

    IssueStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public static boolean isClosed(Integer statusCode){
        return CLOSED.getStatusCode().equals(statusCode);
    }
}
