package org.bytedeco.cuda.cpp;

import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface TemplateResolverByReplacement extends TemplateResolver
{
    class Builder extends TemplateResolverByReplacement_Builder
    {
    }

    String template();

    List<String> replacements();

    @Override
    default boolean isApplicable(String definition)
    {
        return definition.contains(template());
    }

    @Override
    default String resolve(int index, String definition)
    {
        String result = definition;

        String target = template();
        String replacement = replacements().get(index);

        return result.replace(target, replacement);
    }

    @Override
    default int count()
    {
        return replacements().size();
    }
}
