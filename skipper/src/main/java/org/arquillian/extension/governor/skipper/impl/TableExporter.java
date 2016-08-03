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
package org.arquillian.extension.governor.skipper.impl;

import org.arquillian.extension.governor.skipper.api.Status;
import org.arquillian.extension.governor.skipper.api.TestSpec;
import org.arquillian.recorder.reporter.model.entry.table.TableCellEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableEntry;
import org.arquillian.recorder.reporter.model.entry.table.TableRowEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class TableExporter {

    public TableEntry constructReportTable(List<TestSpec> testSpecs) {
        final TableEntry tableEntry = new TableEntry();

        final TableRowEntry pendingRow = constructPendingRow(testSpecs);

        tableEntry.getTableBody().addRow(pendingRow);

        // manual first

        for (final TestSpec testSpec : testSpecs) {
            if (testSpec.status() == Status.MANUAL) {
                for (final TableRowEntry row : constructReportRows(testSpec)) {
                    tableEntry.getTableBody().addRow(row);
                }

                addDummyRow(tableEntry);
            }
        }

        // automated after

        for (final TestSpec testSpec : testSpecs) {
            if (testSpec.status() == Status.AUTOMATED) {
                for (final TableRowEntry row : constructReportRows(testSpec)) {
                    tableEntry.getTableBody().addRow(row);
                }

                addDummyRow(tableEntry);
            }
        }

        return tableEntry;
    }

    private void addDummyRow(TableEntry tableEntry) {
        final TableRowEntry dummyRow = new TableRowEntry();
        dummyRow.addCell(new TableCellEntry("---------------"));
        dummyRow.addCell(new TableCellEntry("---------------"));
        tableEntry.getTableBody().addRow(dummyRow); // dummy row to separate reports
    }

    private TableRowEntry constructPendingRow(List<TestSpec> testSpecs) {
        final TableRowEntry row = new TableRowEntry();

        int pending = 0;

        for (final TestSpec testSpec : testSpecs) {
            if (testSpec.status() == Status.MANUAL) {
                pending++;
            }
        }

        row.addCells(new TableCellEntry("pending"), new TableCellEntry(String.valueOf(pending + "/" + testSpecs.size())));

        return row;
    }

    private List<TableRowEntry> constructReportRows(TestSpec testSpec) {
        final List<TableRowEntry> rows = new ArrayList<TableRowEntry>();

        final TableRowEntry featureRow = new TableRowEntry();
        featureRow.addCells(new TableCellEntry("feature"), new TableCellEntry(testSpec.feature()));

        final TableRowEntry testRow = new TableRowEntry();
        testRow.addCells(new TableCellEntry("test"), new TableCellEntry(testSpec.test()));

        final TableRowEntry prerequisities = new TableRowEntry();
        prerequisities.addCells(new TableCellEntry("prerequisities"), new TableCellEntry(testSpec.prerequisites()));

        final List<TableRowEntry> stepRows = new ArrayList<TableRowEntry>();

        final List<String> stepsList = new ArrayList<String>(Arrays.asList(testSpec.steps()));

        if (!stepsList.isEmpty() && !stepsList.get(0).isEmpty()) {
            final TableRowEntry stepRow = new TableRowEntry();
            stepRow.addCells(new TableCellEntry("steps"), new TableCellEntry(stepsList.get(0)));
            stepRows.add(stepRow);
            stepsList.remove(0);
            for (final String step : stepsList) {
                final TableRowEntry stepRowEntry = new TableRowEntry();
                stepRowEntry.addCells(new TableCellEntry(""), new TableCellEntry(step));
                stepRows.add(stepRowEntry);
            }
        }

        final TableRowEntry assertion = new TableRowEntry();
        assertion.addCells(new TableCellEntry("assertion"), new TableCellEntry(testSpec.assertion()));

        final TableRowEntry issue = new TableRowEntry();
        issue.addCells(new TableCellEntry("issue"), new TableCellEntry(testSpec.issue()));

        final TableRowEntry status = new TableRowEntry();
        status.addCells(new TableCellEntry("status"), new TableCellEntry(testSpec.status().toString()));

        final TableRowEntry author = new TableRowEntry();
        author.addCells(new TableCellEntry("author"), new TableCellEntry(testSpec.author()));

        rows.add(featureRow);
        rows.add(testRow);
        rows.add(prerequisities);
        rows.addAll(stepRows);
        rows.add(assertion);
        rows.add(issue);
        rows.add(status);
        rows.add(author);

        return rows;
    }
}
