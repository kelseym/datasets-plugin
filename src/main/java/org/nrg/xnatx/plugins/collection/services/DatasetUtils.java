package org.nrg.xnatx.plugins.collection.services;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomUtils;

public class DatasetUtils {
    public static <T> Map<String, List<T>> partition(final Collection<T> items, final Map<String, Integer> splits) {
        // Check percentage adds up
        if (sum(splits) != 100) {
            throw new RuntimeException("Percentages must add up to 100");
        }
        final ArrayListMultimap<String, T> partitions = ArrayListMultimap.create();
        final List<T>                      remaining  = new ArrayList<>(items);
        for (final String partitionName : splits.keySet()) {
            final int numItems = getPercentage(items.size(), splits.get(partitionName));
            for (int index = 0; index < numItems; index++) {
                partitions.put(partitionName, remaining.remove(RandomUtils.nextInt(0, remaining.size())));
            }
        }
        return Maps.transformValues(partitions.asMap(), new Function<Collection<T>, List<T>>() {
            @Override
            public List<T> apply(final Collection<T> collection) {
                return new ArrayList<>(collection);
            }
        });
    }

    private static int sum(final Map<String, Integer> splits) {
        int total = 0;
        for (final int split : splits.values()) {
            total += split;
        }
        return total;
    }

    private static int getPercentage(final int total, final int percentage) {
        return (int) (percentage * 100.0 / total + 0.5);
    }
}
