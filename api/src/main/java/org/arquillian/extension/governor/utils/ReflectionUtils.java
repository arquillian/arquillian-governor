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
package org.arquillian.extension.governor.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ReflectionUtils {

    private static final Logger logger = Logger.getLogger(ReflectionUtils.class.getName());

    private ReflectionUtils() {
    }

    public static String getAnnotationValue(Annotation annotation) {
        return getAnnotationProperty(annotation, "value", String.class);
    }

    public static <T> T getAnnotationProperty(Annotation annotation, String name, Class<T> clazz) {
        for (final Method method : annotation.annotationType().getDeclaredMethods()) {
            if (name.equals(method.getName())) {
                try {
                    return (T) method.invoke(annotation);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, String.format("Invocation of method \"%s\" on annotation %s failed",
                            name, annotation.annotationType().getName()), e);
                }
            }
        }
        return null;
    }
}
