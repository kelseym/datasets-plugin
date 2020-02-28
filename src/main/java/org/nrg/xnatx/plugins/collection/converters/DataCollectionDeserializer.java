package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.nrg.xdat.model.XnatxDatacollectionI;

public class DataCollectionDeserializer extends JsonDeserializer<XnatxDatacollectionI> {
    @Override
    public XnatxDatacollectionI deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) {
        return null;
    }
}
