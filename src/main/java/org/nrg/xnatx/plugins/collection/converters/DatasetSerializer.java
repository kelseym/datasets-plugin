/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.converters.DatasetSerializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public abstract class DatasetSerializer<T extends BaseElement> extends StdSerializer<T> {
    protected DatasetSerializer(final Class<T> datasetClass) {
        super(datasetClass);
    }

    protected void writeMetadata(final JsonGenerator generator, final T element) throws IOException {
        writeUsernameField(generator, "insertUser", element.getInsertUser());
        writeNonNullDate(generator, "insertDate", element.getInsertDate());
        writeUsernameField(generator, "lastModifiedUser", element.getItem().getUser());
        writeNonNullDate(generator, "lastModified", element.getItem().getLastModified());
        writeNonBlankField(generator, "xsiType", element.getXSIType());
    }

    protected void writeUsernameField(final JsonGenerator generator, final String name, final UserI user) throws IOException {
        if (user != null) {
            final String username = user.getUsername();
            if (StringUtils.isNotBlank(username)) {
                generator.writeStringField(name, username);
            }
        }
    }

    protected void writeNonBlankField(final JsonGenerator generator, final String name, final String value) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            generator.writeStringField(name, value);
        }
    }

    /**
     * Writes a field named <b>name</b> with the the value inserted directly as JSON, i.e. no escaping of significant characters.
     * This allows the "conversion" of JSON stored as a string into JSON directly in the serialized output.
     *
     * @param generator The generator for the serialization operation.
     * @param name      The name of the field to write.
     * @param value     The value to be inserted.
     *
     * @throws IOException When an error occurs during the serialization write operations.
     */
    protected void writeNonBlankJson(final JsonGenerator generator, final String name, final String value) throws IOException {
        if (StringUtils.isNotBlank(value)) {
            generator.writeFieldName(name);
            generator.writeRawValue(value);
        }
    }

    protected void writeNonNullField(final JsonGenerator generator, final String name, final Object value) throws IOException {
        if (value != null) {
            generator.writeObjectField(name, value);
        }
    }

    @SuppressWarnings("unused")
    protected void writeNonNullBoolean(final JsonGenerator generator, final String name, final Boolean value) throws IOException {
        if (value != null) {
            generator.writeBooleanField(name, value);
        }
    }

    @SuppressWarnings("unused")
    protected void writeNonNullDate(final JsonGenerator generator, final String name, final Date date) throws IOException {
        if (date != null) {
            generator.writeStringField(name, DateUtils.format(date, DATE_FORMAT));
        }
    }

    @SuppressWarnings("unused")
    protected void writeNonNullNumber(final JsonGenerator generator, final String name, final Number value) throws IOException {
        if (value != null) {
            if (value instanceof Integer) {
                generator.writeNumberField(name, (Integer) value);
            } else if (value instanceof Long) {
                generator.writeNumberField(name, (Long) value);
            } else if (value instanceof Float) {
                generator.writeNumberField(name, (float) value);
            } else if (value instanceof Double) {
                generator.writeNumberField(name, (Double) value);
            } else if (value instanceof Short) {
                generator.writeNumberField(name, (Short) value);
            } else {
                generator.writeStringField(name, value.toString());
            }
        }
    }

    protected void writeResourceListField(final JsonGenerator generator, final String name, final List<XnatAbstractresourceI> resources) throws IOException {
        if (resources != null && !resources.isEmpty()) {
            generator.writeArrayFieldStart(name);
            for (final XnatAbstractresourceI resource : resources) {
                generator.writeNumber(resource.getXnatAbstractresourceId());
            }
            generator.writeEndArray();
        }
    }

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
}
