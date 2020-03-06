package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.nrg.xdat.om.SetsCriterion;

public class DataCollectionCriterionDeserializer extends JsonDeserializer<SetsCriterion> {
    @Override
    public SetsCriterion deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) {
        return null;
    }
}
