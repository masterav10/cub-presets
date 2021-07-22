package org.bytedeco.cuda.cpp;

public interface TemplateResolver
{
    boolean isApplicable(String definition);

    String resolve(int count, String definition);

    int count();
}
