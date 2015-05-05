/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.governor.skipper.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.arquillian.extension.governor.skipper.api.Status;
import org.arquillian.extension.governor.skipper.api.TestSpec;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AsciidocExporter {

    private OutputStream outputStream;

    private BufferedWriter writer;

    private File outputFile;

    private final List<String> outputLines = new ArrayList<String>();

    public AsciidocExporter(OutputStream outputStream) {
        this.outputStream = outputStream;
        createWriter();
    }

    public AsciidocExporter(File outputFile) {
        this.outputFile = outputFile;
        createWriter();
    }

    private void createWriter() {
        if (outputStream != null) {
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        } else if (outputFile != null) {
            try {
                writer = new BufferedWriter(new FileWriter(outputFile));
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Unable to create a writer to file %s.", outputFile.getAbsolutePath()));
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public void append(List<TestSpec> testSpecs, String className) {
        int automated = getCount(testSpecs, Status.AUTOMATED);
        int manual = getCount(testSpecs, Status.MANUAL);

        List<String> appending = new ArrayList<String>();
        appending.add("---");
        appending.add("[cols=\"2*\", options=\"header\"]");
        appending.add("|===");
        appending.add("|class|" + className);
        appending.add("|automated|" + automated);
        appending.add("|manual|" + manual);
        appending.add("|===");

        for (TestSpec testSpec : sortTestSpecs(testSpecs)) {
            appending.addAll(reportTestSpec(testSpec));
        }

        outputLines.addAll(appending);
    }

    public void export() {

        try {
            for (String line : outputLines) {
                writer.append(line + "\n");
            }

            writer.flush();
            writer.close();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to export to asciidoctor.");
        }
    }

    // helpers

    private Collection<? extends String> reportTestSpec(TestSpec testSpec) {
        List<String> report = new ArrayList<String>();

        report.add("[cols=\"2*\", options=\"header\"]");
        report.add("|===");
        report.add("|feature|" + testSpec.feature());
        report.add("|test|" + testSpec.test());
        report.add("|prerequisities|" + testSpec.prerequisites());
        report.add("|steps|" + testSpec.steps());
        report.add("|assertion|" + testSpec.assertion());
        report.add("|issue|" + testSpec.issue());
        report.add("|status|" + testSpec.status());
        report.add("|author|" + testSpec.author());
        report.add("|===");

        return report;
    }



    private int getCount(List<TestSpec> testSpecs, Status status) {
        int count = 0;

        for (TestSpec testSpec : testSpecs) {
            if (testSpec.status() == status) {
                count++;
            }
        }

        return count;
    }

    private List<TestSpec> sortTestSpecs(List<TestSpec> testSpecs) {

        List<TestSpec> sortedTestSpecs = new ArrayList<TestSpec>();

        for (TestSpec testSpec : testSpecs) {
            if (testSpec.status() == Status.MANUAL) {
                sortedTestSpecs.add(testSpec);
            }
        }

        // automated after

        for (TestSpec testSpec : testSpecs) {
            if (testSpec.status() == Status.AUTOMATED) {
                sortedTestSpecs.add(testSpec);
            }
        }

        return sortedTestSpecs;
    }
}