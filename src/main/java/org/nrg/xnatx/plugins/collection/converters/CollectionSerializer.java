package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
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
        writeNonBlankField(generator, "project", collection.getProject());
        writeNonBlankField(generator, "definition", collection.getDefinitionId());
        writeNonNullField(generator, "fileCount", collection.getFilecount());
        writeNonNullField(generator, "fileSize", collection.getFilesize());

        // TODO: This should actually be a JsonNode or something injected directly.
        writeNonBlankField(generator, "files", collection.getFiles());

        generator.writeEndObject();
    }
}
