package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public abstract class DatasetDeserializer<T> extends StdDeserializer<T> {
    protected DatasetDeserializer(final Class<T> datasetClass) {
        super(datasetClass);
    }
}
