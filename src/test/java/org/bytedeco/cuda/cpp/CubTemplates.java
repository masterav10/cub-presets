package org.bytedeco.cuda.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.internal.Iterables;
import org.assertj.core.internal.Iterators;
import org.bytedeco.cuda.cpp.MultiListIndexIterator.Result;

public class CubTemplates
{
    private static final String[] POINTER_TYPES;
    private static final String[] VALUE_TYPES;
    private static final String[] COUNTER_TYPES;
    private static final String[] OFFSET_TYPES;

    private static final String[] CHANNEL_VALUES;
    private static final String[] CHANNEL_SUFFIX;

    static
    {
        VALUE_TYPES = new String[]
        { "float", "int" };

        POINTER_TYPES = new String[VALUE_TYPES.length];
        Arrays.setAll(POINTER_TYPES, i -> VALUE_TYPES[i] + "*");

        OFFSET_TYPES = new String[]
        { "int" };

        COUNTER_TYPES = new String[OFFSET_TYPES.length];
        Arrays.setAll(COUNTER_TYPES, i -> "unsigned " + OFFSET_TYPES[i]);

        CHANNEL_VALUES = new String[]
        { "1", "2", "3", "4" };

        CHANNEL_SUFFIX = new String[CHANNEL_VALUES.length];
        Arrays.setAll(CHANNEL_SUFFIX, i -> CHANNEL_VALUES[i] + "Channel");
    }

    private static TemplateResolverByReplacement.Builder byReplacement()
    {
        return new TemplateResolverByReplacement.Builder();
    }

    private static TemplateResolverAggregator aggregate(TemplateResolver r1, TemplateResolver r2,
            TemplateResolver... resolvers)
    {
        return new TemplateResolverAggregator.Builder().addAggregates(r1)
                                                       .addAggregates(r2)
                                                       .addAggregates(resolvers)
                                                       .build();
    }

    private final List<TemplateResolver> templates;

    public CubTemplates()
    {
        this.templates = new ArrayList<>();

        add(channels());
        add(aggregate(SampleIteratorT(), LevelT()));
        add(aggregate(InputIteratorT(), OutputIteratorT()));

        add(CounterT());
        add(OffsetT());
        add(FlagIterator());
        add(NumSelectedIteratorT());
    }

    private static TemplateResolver NumSelectedIteratorT()
    {
        return byReplacement().template("NumSelectedIteratorT")
                              .addReplacements("int*")
                              .build();
    }

    private static TemplateResolver InputIteratorT()
    {
        return byReplacement().template("InputIteratorT")
                              .addReplacements(POINTER_TYPES)
                              .build();
    }

    private static TemplateResolver FlagIterator()
    {
        return byReplacement().template("FlagIterator")
                              .addReplacements("char*")
                              .build();
    }

    private static TemplateResolver OutputIteratorT()
    {
        return byReplacement().template("OutputIteratorT")
                              .addReplacements(POINTER_TYPES)
                              .build();
    }

    private static TemplateResolver channels()
    {
        TemplateResolverAggregator.Builder builder = new TemplateResolverAggregator.Builder();

        return builder.addAggregates(NUM_ACTIVE_CHANNELS())
                      .addAggregates(NUM_CHANNELS())
                      .methodName(new PrefixSuffix.Builder().addSuffix(CHANNEL_SUFFIX)
                                                            .build())
                      .build();
    }

    private static TemplateResolver OffsetT()
    {
        return byReplacement().template("OffsetT")
                              .addReplacements(OFFSET_TYPES)
                              .build();
    }

    private static TemplateResolver CounterT()
    {
        return byReplacement().template("CounterT")
                              .addReplacements(COUNTER_TYPES)
                              .build();
    }

    private static TemplateResolver SampleIteratorT()
    {
        return byReplacement().template("SampleIteratorT")
                              .addReplacements(POINTER_TYPES)
                              .build();
    }

    private static TemplateResolver LevelT()
    {
        return byReplacement().template("LevelT")
                              .addReplacements(VALUE_TYPES)
                              .build();
    }

    private static TemplateResolver NUM_CHANNELS()
    {
        return byReplacement().template("NUM_CHANNELS")
                              .addReplacements(CHANNEL_VALUES)
                              .build();
    }

    private static TemplateResolver NUM_ACTIVE_CHANNELS()
    {
        return byReplacement().template("NUM_ACTIVE_CHANNELS")
                              .addReplacements(CHANNEL_VALUES)
                              .build();
    }

    public Iterable<Result> walk(FunctionDefinition function)
    {
        final String definition = function.toDefinition();

        List<TemplateResolver> temp = templates.stream()
                                               .filter(tr -> tr.isApplicable(definition))
                                               .collect(Collectors.toList());

        return () -> new MultiListIndexIterator(function, temp);
    }

    private void add(TemplateResolver resolver)
    {
        this.templates.add(resolver);
    }
}
