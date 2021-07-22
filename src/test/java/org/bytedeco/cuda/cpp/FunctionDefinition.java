package org.bytedeco.cuda.cpp;

import java.util.List;

import org.inferred.freebuilder.FreeBuilder;

@FreeBuilder
public interface FunctionDefinition
{
    class Builder extends FunctionDefinition_Builder
    {

    }

    List<String> templateTypes();

    List<String> templates();

    String name();
}
