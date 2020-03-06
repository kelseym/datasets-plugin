package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xdat.om.SetsCriterion;

public class DataCollectionCriterionSerializer extends StdSerializer<SetsCriterion> {
    public DataCollectionCriterionSerializer() {
        super(SetsCriterion.class);
    }

    @Override
    public void serialize(final SetsCriterion criterion, final JsonGenerator generator, final SerializerProvider serializerProvider) {

    }
}
