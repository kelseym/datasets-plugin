package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.nrg.xdat.om.SetsDefinition;

public class DataCollectionDefinitionDeserializer extends StdDeserializer<SetsDefinition> {
    public DataCollectionDefinitionDeserializer() {
        super(SetsDefinition.class);
    }

    @Override
    public SetsDefinition deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) {
        // final String id = jsonParser.get
        return null;
    }
}
