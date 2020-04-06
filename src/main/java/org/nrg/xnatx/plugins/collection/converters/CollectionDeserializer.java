package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.xdat.om.SetsCollection;

public class CollectionDeserializer extends DatasetDeserializer<SetsCollection> {
    public CollectionDeserializer() {
        super(SetsCollection.class);
    }

    @Override
    public SetsCollection deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);

        final SetsCollection collection = node.has("id") ? getInstance(node.get("id")) : new SetsCollection();
        if (node.has("project")) {
            collection.setProject(node.get("project").textValue());
        }
        if (node.has("label")) {
            collection.setLabel(node.get("label").textValue());
        }
        if (node.has("definition")) {
            collection.setDefinitionId(node.get("definition").textValue());
        }
        if (node.has("fileCount")) {
            collection.setFilecount(NumberUtils.toInt(node.get("fileCount").textValue(), 0));
        }
        if (node.has("fileSize")) {
            collection.setFilesize(NumberUtils.toLong(node.get("fileSize").textValue()));
        }
        if (node.has("files")) {
            collection.setFiles(node.get("files").textValue());
        }

        return collection;
    }
}
