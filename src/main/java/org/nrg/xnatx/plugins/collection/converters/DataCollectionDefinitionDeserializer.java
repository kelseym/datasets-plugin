package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.nrg.xdat.model.XnatxDatacollectiondefinitionI;

public class DataCollectionDefinitionDeserializer extends StdDeserializer<XnatxDatacollectiondefinitionI> {
    public DataCollectionDefinitionDeserializer() {
        super(XnatxDatacollectiondefinitionI.class);
    }

    @Override
    public XnatxDatacollectiondefinitionI deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) {
        // final String id = jsonParser.get
        return null;
    }
}
