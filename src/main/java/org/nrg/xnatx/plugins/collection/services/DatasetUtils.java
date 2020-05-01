package org.nrg.xnatx.plugins.collection.services;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.RandomUtils;

public class DatasetUtils {
    /**
     * This compares all of the entries in the submitted map and returns the key for the entry that
     * has the largest value as determined by calling the <b>compareTo()</b> method on the comparable
     * objects.
     *
     * @param map The map to be searched.
     * @param <K> The type of the map key.
     * @param <V> The type of the map value.
     *
     * @return The key for the entry with the maximum value.
     */
    public static <K, V extends Comparable<V>> Map.Entry<K, V> findMaxValueEntry(final Map<K, V> map) {
        return Collections.max(map.entrySet(), new Comparator<Map.Entry<K, V>>() {
            public int compare(final Map.Entry<K, V> entry1, final Map.Entry<K, V> entry2) {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });
    }

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
