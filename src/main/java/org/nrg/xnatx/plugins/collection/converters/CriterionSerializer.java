/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.converters.CriterionSerializer
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
import org.nrg.xdat.om.SetsCriterion;

public class CriterionSerializer extends DatasetSerializer<SetsCriterion> {
    public CriterionSerializer() {
        super(SetsCriterion.class);
    }

    @Override
    public void serialize(final SetsCriterion criterion, final JsonGenerator generator, final SerializerProvider serializerProvider) throws IOException {
        generator.writeStartObject();
        writeNonBlankField(generator, "resolver", criterion.getResolver());
        writeNonBlankField(generator, "payload", criterion.getPayload());
        generator.writeEndObject();
    }
}
