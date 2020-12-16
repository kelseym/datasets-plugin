/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.converters.CriterionDeserializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import org.nrg.xdat.om.SetsCriterion;

public class CriterionDeserializer extends DatasetDeserializer<SetsCriterion> {
    public CriterionDeserializer() {
        super(SetsCriterion.class);
    }

    @Override
    public SetsCriterion deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("invalid start marker");
        }

        final SetsCriterion criterion = new SetsCriterion();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            final String field = parser.getCurrentName();
            parser.nextToken();  //move to next token in string
            switch (field) {
                case "resolver":
                    criterion.setResolver(parser.getText());
                    break;
                case "payload":
                    criterion.setPayload(stringify(parser, context));
                    break;
            }
        }
        return criterion;
    }
}
