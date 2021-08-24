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
        int start = definition.indexOf('<') + 1;
        int end = definition.indexOf('>');

        String[] templates = definition.substring(start, end)
                                       .split(",");

        for (String template : templates)
        {
            if (template.equals(template()))
            {
                return true;
            }
        }

        return false;
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
