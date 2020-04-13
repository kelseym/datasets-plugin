package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;

@Slf4j
public class DefinitionDeserializer extends DatasetDeserializer<SetsDefinition> {
    public DefinitionDeserializer() {
        super(SetsDefinition.class);
    }

    @Override
    public SetsDefinition deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new IOException("invalid start marker");
        }

        final SetsDefinition definition = new SetsDefinition();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            final String field = parser.getCurrentName();
            parser.nextToken();  //move to next token in string
            switch (field) {
                case "id":
                    definition.setId(parser.getText());
                    break;
                case "project":
                    definition.setProject(parser.getText());
                    break;
                case "label":
                    definition.setLabel(parser.getText());
                    break;
                case "description":
                    definition.setDescription(parser.getText());
                    break;
                case "criteria":
                    if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
                        throw new IOException("Invalid start marker for bs property");
                    }
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        final SetsCriterion criterion = context.readValue(parser, SetsCriterion.class);
                        try {
                            definition.addCriteria(criterion);
                        } catch (Exception e) {
                            log.error("An error occurred trying to add the criterion: {}", criterion);
                        }
                    }
                    break;
            }
        }
        return definition;
    }
}
