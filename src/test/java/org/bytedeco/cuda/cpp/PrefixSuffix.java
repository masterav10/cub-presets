package org.bytedeco.cuda.cpp;

import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface PrefixSuffix extends TemplateResolver
{
    class Builder extends PrefixSuffix_Builder
    {
    }

    List<String> prefix();

    List<String> suffix();

    @Override
    default boolean isApplicable(String definition)
    {
        return true;
    }

    @Override
    default String resolve(int index, String definition)
    {
        String prefix = index >= prefix().size() ? "" : prefix().get(index);
        String suffix = index >= suffix().size() ? "" : suffix().get(index);

        return prefix + definition + suffix;
    }

    @Override
    default int count()
    {
        return Math.max(prefix().size(), suffix().size());
    }
}
