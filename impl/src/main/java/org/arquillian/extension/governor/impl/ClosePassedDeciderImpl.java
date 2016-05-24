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

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mbasovni@redhat.com">Martin Basovnik</a>
 */
public class ClosePassedDeciderImpl implements ClosePassedDecider {

    private Map<Annotation, Boolean> closableAnnotationMap = new HashMap<Annotation, Boolean>();

    @Override
    public Map<Annotation, Boolean> get() {
        return closableAnnotationMap;
    }

    @Override
    public void setClosable(Annotation annotation, boolean closeable) {
        Boolean old = closableAnnotationMap.get(annotation);
        closableAnnotationMap.put(annotation, old == null ? closeable : (old.booleanValue() & closeable));
    }

    @Override
    public boolean isCloseable(Annotation annotation) {
        return closableAnnotationMap.get(annotation);
    }
}
