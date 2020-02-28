package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xdat.model.XnatxDatacollectioncriterionI;

public class DataCollectionCriterionSerializer extends StdSerializer<XnatxDatacollectioncriterionI> {
    public DataCollectionCriterionSerializer() {
        super(XnatxDatacollectioncriterionI.class);
    }

    @Override
    public void serialize(final XnatxDatacollectioncriterionI criterion, final JsonGenerator generator, final SerializerProvider serializerProvider) {

    }
}
