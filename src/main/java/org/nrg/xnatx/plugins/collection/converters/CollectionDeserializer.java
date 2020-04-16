package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import org.nrg.xdat.om.SetsCollection;

public class CollectionDeserializer extends DatasetDeserializer<SetsCollection> {
    public CollectionDeserializer() {
        super(SetsCollection.class);
    }

    @Override
    public SetsCollection deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("invalid start marker");
        }

        final SetsCollection collection = new SetsCollection();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            final String field = parser.getCurrentName();
            parser.nextToken();
            switch (field) {
                case "project":
                    collection.setProject(parser.getText());
                    break;
                case "label":
                    collection.setLabel(parser.getText());
                    break;
                case "definition":
                    collection.setDefinitionId(parser.getText());
                    break;
                case "fileCount":
                    collection.setFilecount(parser.getIntValue());
                    break;
                case "fileSize":
                    collection.setFilesize(parser.getLongValue());
                    break;
                case "files":
                    collection.setFiles(parser.getValueAsString());
                    break;
            }
        }
        return collection;
    }
}
