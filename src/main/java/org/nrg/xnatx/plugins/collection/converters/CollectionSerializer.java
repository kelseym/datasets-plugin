/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.converters.CollectionSerializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.List;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.SetsCollection;

public class CollectionSerializer extends DatasetSerializer<SetsCollection> {
    public CollectionSerializer() {
        super(SetsCollection.class);
    }

    @Override
    public void serialize(final SetsCollection collection, final JsonGenerator generator, final SerializerProvider serializer) throws IOException {
        generator.writeStartObject();
        writeNonBlankField(generator, "id", collection.getId());
        writeNonBlankField(generator, "label", collection.getLabel());
        writeNonBlankField(generator, "description", collection.getDescription());
        writeNonBlankField(generator, "note", collection.getNote());
        writeNonBlankField(generator, "project", collection.getProject());
        writeNonBlankField(generator, "definition", collection.getDefinitionId());
        writeNonNullField(generator, "fileCount", collection.getFilecount());
        writeNonNullField(generator, "fileSize", collection.getFilesize());
        writeNonBlankJson(generator, "files", collection.getFiles());

        final List<XnatAbstractresourceI> resources = collection.getResources_resource();
        if (resources != null && !resources.isEmpty()) {
            generator.writeArrayFieldStart("resources");
            for (final XnatAbstractresourceI resource : resources) {
                generator.writeStartObject();
                generator.writeNumberField("id", resource.getXnatAbstractresourceId());
                generator.writeStringField("label", resource.getLabel());
                generator.writeEndObject();
            }
            generator.writeEndArray();
        }

        generator.writeEndObject();
    }
}
