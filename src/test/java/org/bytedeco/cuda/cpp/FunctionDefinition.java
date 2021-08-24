package org.bytedeco.cuda.cpp;

import java.util.List;
import java.util.Optional;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface FunctionDefinition
{
    class Builder extends FunctionDefinition_Builder
    {

    }

    Optional<String> namespace();

    Optional<String> struct();

    List<String> templateTypes();

    List<String> templates();

    String name();

    default String toDefinition()
    {
        StringBuilder builder = new StringBuilder();

        namespace().ifPresent(ns -> builder.append(ns)
                                           .append("::"));

        struct().ifPresent(st -> builder.append(st)
                                        .append("::"));

        builder.append(name());
        builder.append(templates().stream()
                                  .reduce((a, b) -> a + "," + b)
                                  .map(s -> "<" + s + ">")
                                  .orElse(""));

        return builder.toString();
    }
}
