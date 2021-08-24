package org.bytedeco.cuda.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.bytedeco.cuda.cpp.MultiListIndexIterator.Result;

/**
 * Loops through all combinations of the provided iterators.
 * 
 * @author Dan Avila
 *
 */
public class MultiListIndexIterator implements Iterator<Result>
{
    private static final Set<String> IGNORE_METHODS = new HashSet<>(Arrays.asList("ArgMin", "ArgMax"));

    public static final class Result
    {
        private final String newDefinition;
        private final String newFunctionName;
        private final FunctionDefinition definition;

        public Result(String newDefinition, String newFunctionName, FunctionDefinition definition)
        {
            this.newDefinition = newDefinition;
            this.newFunctionName = newFunctionName;
            this.definition = definition;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();

            boolean anyTemplateUnresolved = this.definition.templates()
                                                           .stream()
                                                           .anyMatch(template -> newDefinition.contains(template));

            if (anyTemplateUnresolved || IGNORE_METHODS.contains(newFunctionName))
            {
                builder.append("// ");
            }

            builder.append("infoMap.put(new Info(")
                   .append('"')
                   .append(newDefinition)
                   .append('"')
                   .append(").javaNames(")
                   .append('"')
                   .append(newFunctionName)
                   .append('"')
                   .append("));");

            return builder.toString();
        }
    }

    private final List<TemplateResolver> resolvers;

    private final FunctionDefinition definition;
    private final int[] indexes;

    private final int total;
    private int count;

    private final LinkedHashMap<String, String> uniqueDefinitions = new LinkedHashMap<>();

    public MultiListIndexIterator(FunctionDefinition definition, List<TemplateResolver> resolvers)
    {
        this.definition = definition;
        this.resolvers = new ArrayList<>(resolvers);

        this.indexes = new int[this.resolvers.size()];

        this.total = this.resolvers.stream()
                                   .mapToInt(TemplateResolver::count)
                                   .reduce((a, b) -> a * b)
                                   .orElse(0);
        this.count = 0;
    }

    public MultiListIndexIterator(FunctionDefinition definition, TemplateResolver... remaining)
    {
        this(definition, Arrays.asList(remaining));
    }

    public boolean hasNext()
    {
        while (count < total)
        {
            String newDefinition = definition.toDefinition();
            String newMethodName = definition.name();

            for (int i = 0; i < resolvers.size(); i++)
            {
                int index = this.indexes[i];
                TemplateResolver resolver = this.resolvers.get(i);

                newDefinition = resolver.resolve(index, newDefinition);

                Optional<TemplateResolver> optional = resolver.methodName();

                if (optional.isPresent())
                {
                    TemplateResolver functionResolver = optional.get();
                    newMethodName = functionResolver.resolve(index, newMethodName);
                }
            }

            adjustIndexArray(0);
            count++;

            if (!this.uniqueDefinitions.containsKey(newDefinition))
            {
                this.uniqueDefinitions.put(newDefinition, newMethodName);
                return true;
            }
        }

        return false;
    }

    public Result next()
    {
        String newDefinition = null;
        String newMethodName = null;

        for (Entry<String, String> entry : this.uniqueDefinitions.entrySet())
        {
            newDefinition = entry.getKey();
            newMethodName = entry.getValue();
        }

        return new Result(newDefinition, newMethodName, definition);
    }

    private void adjustIndexArray(int index)
    {
        if (index < this.resolvers.size())
        {
            TemplateResolver resolver = this.resolvers.get(index);
            int newVal = this.indexes[index] + 1;

            if (newVal >= resolver.count())
            {
                newVal = 0;
                adjustIndexArray(index + 1);
            }

            this.indexes[index] = newVal;
        }
    }
}
