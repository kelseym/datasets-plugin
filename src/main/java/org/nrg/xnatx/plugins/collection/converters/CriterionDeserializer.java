package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.nrg.xdat.om.SetsCriterion;

public class CriterionDeserializer extends DatasetDeserializer<SetsCriterion> {
    public CriterionDeserializer() {
        super(SetsCriterion.class);
    }

    @Override
    public SetsCriterion deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);
        final SetsCriterion criterion = new SetsCriterion();
        criterion.setResolver(node.get("resolver").textValue());
        criterion.setPayload(node.get("payload").textValue());
        return criterion;
    }
}
