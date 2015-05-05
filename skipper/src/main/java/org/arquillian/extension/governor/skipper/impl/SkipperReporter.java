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

import java.io.File;
import java.util.List;

import org.arquillian.extension.governor.skipper.api.TestSpec;
import org.arquillian.extension.governor.skipper.config.SkipperConfiguration;
import org.arquillian.recorder.reporter.ReporterConfiguration;
import org.arquillian.recorder.reporter.event.PropertyReportEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SkipperReporter {

    @Inject
    private Instance<SkipperReportHolder> holder;

    @Inject
    private Instance<ReporterConfiguration> reporterConfiguration;

    @Inject
    private Instance<SkipperConfiguration> skipperConfiguration;

    @Inject
    private Event<PropertyReportEvent> propertyReportEvent;

    @Inject
    @ApplicationScoped
    private InstanceProducer<AsciidocExporter> asciiDocExporter;

    public void on(@Observes BeforeSuite beforeSuite) {
        SkipperConfiguration skipperConfiguration = this.skipperConfiguration.get();
        ReporterConfiguration reporterConfiguration = this.reporterConfiguration.get();

        File adocExportFile = null;

        if (skipperConfiguration.getPlainAdoc() != null) {
            adocExportFile = new File(reporterConfiguration.getFile().getParentFile(), skipperConfiguration.getPlainAdoc());
        }

        if (adocExportFile != null) {
            AsciidocExporter asciidocExporter = new AsciidocExporter(adocExportFile);
            this.asciiDocExporter.set(asciidocExporter);
        }
    }

    public void on(@Observes AfterClass afterClass) {

        List<TestSpec> testSpecs = holder.get().getAll();

        if (this.asciiDocExporter.get() != null) {
            this.asciiDocExporter.get().append(testSpecs, afterClass.getTestClass().getJavaClass().getCanonicalName());
        }

        propertyReportEvent.fire(new PropertyReportEvent(new TableExporter().constructReportTable(testSpecs)));

        holder.get().clear();
    }

    public void on(@Observes AfterSuite afterSuite) {

        if (this.asciiDocExporter.get() != null) {
            this.asciiDocExporter.get().export();
        }
    }

}