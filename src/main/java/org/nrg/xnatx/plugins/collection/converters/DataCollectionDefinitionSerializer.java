package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsDefinition;

public class DataCollectionDefinitionSerializer extends StdSerializer<SetsDefinition> {
    public DataCollectionDefinitionSerializer() {
        super(SetsDefinition.class);
    }

    @Override
    public void serialize(final SetsDefinition definition, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        writeNonBlankField(generator, "id", definition.getId());
        writeNonBlankField(generator, "label", definition.getLabel());
        writeNonBlankField(generator, "description", definition.getDescription());
        writeNonBlankField(generator, "project", definition.getProject());
        generator.writeArrayFieldStart("criteria");
        for (final SetsCriterionI criterion : definition.getCriteria()) {
            generator.writeStartObject();
            generator.writeStringField("resolver", criterion.getResolver());
            generator.writeStringField("payload", criterion.getPayload());
            generator.writeEndObject();
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

    private static void writeNonBlankField(final JsonGenerator generator, final String name, final String value) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            generator.writeStringField(name, value);
        }
    }
}
