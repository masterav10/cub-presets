package org.bytedeco.cuda.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Loops through all combinations of the provided iterators.
 * 
 * @author Dan Avila
 *
 */
public class MultiListIndexIterator implements Iterator<String>
{
    private final List<TemplateResolver> resolvers;
    private final String definition;
    private final int[] indexes;

    private final int total;
    private int count;

    public MultiListIndexIterator(String definition, List<TemplateResolver> resolvers)
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

    public MultiListIndexIterator(String definition, TemplateResolver... remaining)
    {
        this(definition, Arrays.asList(remaining));
    }

    public boolean hasNext()
    {
        return count < total;
    }

    public String next()
    {
        String newDefinition = definition;

        for (int i = 0; i < resolvers.size(); i++)
        {
            int index = this.indexes[i];
            TemplateResolver resolver = this.resolvers.get(i);

            newDefinition = resolver.resolve(index, newDefinition);
        }

        adjustIndexArray(0);
        this.count++;

        return newDefinition;
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
