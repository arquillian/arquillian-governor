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
package org.arquillian.extension.governor.api.detector.utils;

import org.apache.commons.lang3.SystemUtils;
import org.arquillian.extension.governor.api.detector.Detectable;

/**
 * Class {@code OS} encapsulates classes for detecting operation systems.
 *
 * @author <a href="mailto:mbasovni@redhat.com">Martin Basovnik</a>
 *
 */
public final class OS {

    private OS() {
    }

    public static final class Unix implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_UNIX;
        }
    }

    public static final class Linux implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_LINUX;
        }
    }

    public static final class Solaris implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_SOLARIS;
        }
    }

    public static final class HPUX implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_HP_UX;
        }
    }

    public static final class IRIX implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_IRIX;
        }
    }

    public static final class SunOS implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_SUN_OS;
        }
    }

    public static final class Windows implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_WINDOWS;
        }
    }

    public static final class Mac implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_MAC;
        }
    }

    public static final class AIX implements Detectable {
        @Override
        public boolean detected() {
            return SystemUtils.IS_OS_AIX;
        }
    }
}
