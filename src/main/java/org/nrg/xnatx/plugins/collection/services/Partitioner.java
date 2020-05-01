package org.nrg.xnatx.plugins.collection.services;

import com.google.common.base.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Partitioner<T> extends ArrayList<T> implements Function<Integer, List<T>> {
    public Partitioner(final List<T> collection) {
        super(collection);
    }

    @Override
    public List<T> apply(final Integer value) {
        return subList(_offset.get(), _offset.addAndGet(value));
    }

    private final AtomicInteger _offset = new AtomicInteger();
}

