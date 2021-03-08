/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.converters.CollectionSerializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.nrg.xdat.om.SetsCollection;

import java.io.IOException;

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

        writeResourceListField(generator, "references", collection.getReferences_resource());
        writeResourceListField(generator, "resources", collection.getResources_resource());

        writeMetadata(generator, collection);

        generator.writeEndObject();
    }
}
