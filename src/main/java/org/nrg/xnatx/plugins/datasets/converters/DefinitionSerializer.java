/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.converters.DefinitionSerializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsDefinition;

public class DefinitionSerializer extends DatasetSerializer<SetsDefinition> {
    public DefinitionSerializer() {
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
            writeNonBlankField(generator, "resolver", criterion.getResolver());
            writeNonBlankJson(generator, "payload", criterion.getPayload());
            generator.writeEndObject();
        }
        generator.writeEndArray();
        writeMetadata(generator, definition);
        generator.writeEndObject();
    }
}
