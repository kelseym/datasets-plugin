/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.converters.DatasetDeserializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DatasetDeserializer<T> extends StdDeserializer<T> {
    protected DatasetDeserializer(final Class<T> datasetClass) {
        super(datasetClass);
    }

    protected String stringify(final JsonParser parser, final DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() == JsonToken.START_ARRAY || parser.getCurrentToken() == JsonToken.START_OBJECT) {
            return context.readValue(parser, JsonNode.class).toString();
        } else if (parser.getCurrentToken() == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE.toString();
        } else if (parser.getCurrentToken() == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE.toString();
        } else if (parser.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        } else if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_FLOAT) {
            return Float.toString(parser.getFloatValue());
        } else if (parser.getCurrentToken() == JsonToken.VALUE_NUMBER_INT) {
            return Integer.toString(parser.getIntValue());
        } else if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
            return parser.getText();
        } else {
            return parser.getValueAsString();
        }
    }
}
