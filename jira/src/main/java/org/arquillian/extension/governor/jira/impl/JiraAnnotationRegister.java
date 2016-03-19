package org.arquillian.extension.governor.jira.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class JiraAnnotationRegister
{
    private static final List<Annotation> annotations = new ArrayList<Annotation>();

    public static void add(Annotation annotation)
    {
        annotations.add(annotation);
    }

    public static boolean contains(Annotation annotation)
    {
        return annotations.contains(annotation);
    }
    
    public static void clear()
    {
        annotations.clear();
    }
}
