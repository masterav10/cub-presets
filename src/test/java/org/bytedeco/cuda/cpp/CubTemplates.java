package org.bytedeco.cuda.cpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CubTemplates
{
    private static final String[] POINTER_TYPES;
    private static final String[] VALUE_TYPES;
    private static final String[] COUNTER_TYPES;
    private static final String[] OFFSET_TYPES;

    static
    {
        VALUE_TYPES = new String[]
        { "float", "int" };

        POINTER_TYPES = new String[VALUE_TYPES.length];
        Arrays.setAll(POINTER_TYPES, i -> VALUE_TYPES[i] + "*");

        OFFSET_TYPES = new String[]
        { "int", "long" };

        COUNTER_TYPES = new String[OFFSET_TYPES.length];
        Arrays.setAll(COUNTER_TYPES, i -> "unsigned " + OFFSET_TYPES[i]);

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

        add(aggregate(NUM_ACTIVE_CHANNELS(), NUM_CHANNELS()));
        add(aggregate(SampleIteratorT(), LevelT()));

        add(CounterT());
        add(OffsetT());
    }

    private static TemplateResolver OffsetT()
    {
        return byReplacement().template("OffsetT")
                              .addReplacements("int")
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
                              .addReplacements("1", "2", "3", "4")
                              .build();
    }

    private static TemplateResolver NUM_ACTIVE_CHANNELS()
    {
        return byReplacement().template("NUM_ACTIVE_CHANNELS")
                              .addReplacements("1", "2", "3", "4")
                              .build();
    }

    public Iterable<String> walk(String definition)
    {
        List<TemplateResolver> temp = templates.stream()
                                               .filter(tr -> tr.isApplicable(definition))
                                               .collect(Collectors.toList());

        return () -> new MultiListIndexIterator(definition, temp);
    }

    private void add(TemplateResolver resolver)
    {
        this.templates.add(resolver);
    }
}
