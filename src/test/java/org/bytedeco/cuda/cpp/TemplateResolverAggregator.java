package org.bytedeco.cuda.cpp;

import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface TemplateResolverAggregator extends TemplateResolver
{
    class Builder extends TemplateResolverAggregator_Builder
    {
    }

    @Override
    default String resolve(int count, String definition)
    {
        String newDef = definition;

        for (TemplateResolver resolver : aggregates())
        {
            newDef = resolver.resolve(count, newDef);
        }

        return newDef;
    }

    @Override
    default boolean isApplicable(String definition)
    {
        for (TemplateResolver resolver : aggregates())
        {
            if (!resolver.isApplicable(definition))
            {
                return false;
            }
        }

        return true;
    }

    List<TemplateResolver> aggregates();

    @Override
    default int count()
    {
        return aggregates().get(0)
                           .count();
    }
}
