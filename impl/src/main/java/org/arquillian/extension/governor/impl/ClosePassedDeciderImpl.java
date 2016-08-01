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
package org.arquillian.extension.governor.impl;

import org.arquillian.extension.governor.api.ClosePassedDecider;
import org.arquillian.extension.governor.utils.RefletionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:mbasovni@redhat.com">Martin Basovnik</a>
 */
public class ClosePassedDeciderImpl implements ClosePassedDecider {

    private static final Logger logger = Logger.getLogger(ClosePassedDeciderImpl.class.getName());

    private Map<Annotation, Boolean> closableAnnotationMap = new HashMap<Annotation, Boolean>();

    @Override
    public Map<Annotation, Boolean> get() {
        return closableAnnotationMap;
    }

    @Override
    public void setClosable(Annotation annotation, boolean closeable) {
        for (Map.Entry<Annotation, Boolean> entry : closableAnnotationMap.entrySet()) {
            Annotation oldAnnotation = entry.getKey();
            if (oldAnnotation.annotationType().equals(annotation.annotationType())) {
                String oldId = RefletionUtils.getAnnotationValue(oldAnnotation);
                String id = RefletionUtils.getAnnotationValue(annotation);
                if (oldId != null && oldId.equals(id)) {
                    closableAnnotationMap.put(oldAnnotation, entry.getValue().booleanValue() & closeable);
                    return;
                }
            }
        }
        closableAnnotationMap.put(annotation, closeable);
    }

    @Override
    public boolean isCloseable(Annotation annotation) {
        return closableAnnotationMap.get(annotation);
    }
}
