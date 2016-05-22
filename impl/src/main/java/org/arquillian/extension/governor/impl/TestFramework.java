package org.arquillian.extension.governor.impl;

/**
 * Supported testing frameworks.
 */
public enum TestFramework {

    JUNIT("org.junit.Test"),
    TESTNG("org.testng.annotations.Test");

    /**
     * Class name.
     */
    private final String className;

    TestFramework(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
