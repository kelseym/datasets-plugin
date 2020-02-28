package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xdat.model.XnatxDatacollectionI;

public class DataCollectionSerializer extends StdSerializer<XnatxDatacollectionI> {
    public DataCollectionSerializer() {
        super(XnatxDatacollectionI.class);
    }

    @Override
    public void serialize(final XnatxDatacollectionI XnatxDatacollectionI, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) {

    }
}
