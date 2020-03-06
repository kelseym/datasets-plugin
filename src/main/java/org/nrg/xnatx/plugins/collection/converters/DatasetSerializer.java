package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

public abstract class DatasetSerializer<T> extends StdSerializer<T> {
    protected DatasetSerializer(final Class<T> datasetClass) {
        super(datasetClass);
    }

    static void writeNonBlankField(final JsonGenerator generator, final String name, final String value) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            generator.writeStringField(name, value);
        }
    }

    static void writeNonNullField(final JsonGenerator generator, final String name, final Object value) throws IOException {
        if (value != null) {
            generator.writeObjectField(name, value);
        }
    }
}
