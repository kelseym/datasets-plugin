package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.nrg.xdat.model.XnatxDatacollectioncriterionI;

public class DataCollectionCriterionDeserializer extends JsonDeserializer<XnatxDatacollectioncriterionI> {
    @Override
    public XnatxDatacollectioncriterionI deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) {
        return null;
    }
}
