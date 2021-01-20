/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.converters.CollectionDeserializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.SetsCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                case "id":
                    collection.setId(parser.getText());
                    break;
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
                case "note":
                    collection.setNote(parser.getText());
                    break;
                case "visit":
                    collection.setVisit(parser.getText());
                    break;
                case "references":
                    final List<Integer> references = parser.readValueAs(RESOURCE_LIST_TYPE_REFERENCE);
                    if (log.isInfoEnabled()) {
                        log.info("Found {} references: {}", references.size(), references.stream().map(Object::toString).collect(Collectors.joining(", ")));
                    }
                    collection.addReferencesById(references);
                    break;
                case "resources":
                    final List<Integer> resources = parser.readValueAs(RESOURCE_LIST_TYPE_REFERENCE);
                    if (log.isInfoEnabled()) {
                        log.info("Found {} resources: {}", resources.size(), resources.stream().map(Object::toString).collect(Collectors.joining(", ")));
                    }
                    collection.addResourcesById(resources);
                    break;
            }
        }
        return collection;
    }

    private static final TypeReference<ArrayList<Integer>> RESOURCE_LIST_TYPE_REFERENCE = new TypeReference<ArrayList<Integer>>() {
    };
}
