package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xdat.om.SetsCollection;

public class DataCollectionSerializer extends StdSerializer<SetsCollection> {
    public DataCollectionSerializer() {
        super(SetsCollection.class);
    }

    @Override
    public void serialize(final SetsCollection SetsCollection, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) {

    }
}
