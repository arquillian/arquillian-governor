package org.arquillian.extension.governor.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RefletionUtils {

    private static final Logger logger = Logger.getLogger(RefletionUtils.class.getName());

    private RefletionUtils() {
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
