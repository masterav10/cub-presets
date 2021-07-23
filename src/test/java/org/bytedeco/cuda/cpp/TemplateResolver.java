package org.bytedeco.cuda.cpp;

import java.util.Optional;

public interface TemplateResolver
{
    boolean isApplicable(String definition);

    int count();

    String resolve(int count, String definition);

    Optional<TemplateResolver> methodName();
}
