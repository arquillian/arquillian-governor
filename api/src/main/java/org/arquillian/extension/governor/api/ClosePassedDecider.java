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
package org.arquillian.extension.governor.api;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Intefrace <tt>ClosePassedDecider</tt> gives us information if we can close issues defined in governor annotations.
 *
 * @author <a href="mailto:mbasovni@redhat.com">Martin Basovnik</a>
 *
 */
public interface ClosePassedDecider
{
    /**
     * Returns {@code Map} with annotations and their closable flags in the scope of the whole test suite.
     * @return {@code Map} with annotations and their closable flags.
     */
    Map<Annotation, Boolean> get();

    /**
     * Sets the closable flag to the issue specified in {@code annotation} in the scope of related test method.
     * @param annotation Governor annotation.
     * @param closeable if {@code true}, the issue specified in annotation can be closed.
     */
    void setClosable(Annotation annotation, boolean closeable);

    /**
     * Returns {@code true} if the issue specified in {@code annotation} can be closed in the scope of the whole test suite.
     * @param annotation Governor annotation.
     * @return {@code true} if the issue specified in annotation can be closed, otherwise {@code false}.
     */
    boolean isCloseable(Annotation annotation);
}
