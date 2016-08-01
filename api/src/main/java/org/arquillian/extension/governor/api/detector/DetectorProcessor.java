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
package org.arquillian.extension.governor.api.detector;

import org.arquillian.extension.governor.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class {@code DetectorProcessor} can process annotations with method {@code detector()} returning type {@link Detector}.
 *
 * @author <a href="mailto:mbasovni@redhat.com">Martin Basovnik</a>
 *
 */
public class DetectorProcessor {

    private static final Logger logger = Logger.getLogger(DetectorProcessor.class.getName());

    public boolean process(Annotation annotation) {
        boolean detected = true;
        try {
            final Detector detector = ReflectionUtils.getAnnotationProperty(annotation, "detector", Detector.class);
            final DeciderStrategy strategy = detector.strategy().newInstance();
            final List<Detectable> detectables = new ArrayList<Detectable>();
            for (final Class<? extends Detectable> detectableClass : detector.value()) {
                detectables.add(detectableClass.newInstance());
            }
            if (strategy instanceof BaseDeciderStrategy) {
                ((BaseDeciderStrategy) strategy).detectables(detectables);
            }
            detected = strategy.resolve();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return detected;
    }
}
