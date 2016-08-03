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
package org.arquillian.extension.governor.api.detector;

/**
 * Interface {@code Strategy} can define merging strategy for objects of type {@link Detectable}.
 *
 * @author <a href="mailto:mbasovni@redhat.com">Martin Basovnik</a>
 */
public interface DeciderStrategy {

    boolean resolve();

    class And extends BaseDeciderStrategy {

        @Override
        public boolean resolve() {
            for (final Detectable detectable : detectables) {
                if (!detectable.detected()) {
                    return false;
                }
            }
            return true;
        }
    }

    class Or extends BaseDeciderStrategy {

        @Override
        public boolean resolve() {
            for (final Detectable detectable : detectables) {
                if (detectable.detected()) {
                    return true;
                }
            }
            return false;
        }
    }
}
