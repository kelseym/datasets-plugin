package org.nrg.xnatx.plugins.collection.converters;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;

@Slf4j
public class DefinitionDeserializer extends DatasetDeserializer<SetsDefinition> {
    public DefinitionDeserializer() {
        super(SetsDefinition.class);
    }

    @Override
    public SetsDefinition deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        final JsonNode node = parser.getCodec().readTree(parser);

        final SetsDefinition definition = node.has("id") ? getDefinition(node.get("id")) : new SetsDefinition();
        if (node.has("project")) {
            definition.setProject(node.get("project").textValue());
        }
        if (node.has("label")) {
            definition.setLabel(node.get("label").textValue());
        }
        if (node.has("description")) {
            definition.setDescription(node.get("description").textValue());
        }
        if (node.has("criteria")) {
            final List<SetsCriterionI> existing = definition.getCriteria();
            if (existing != null && !existing.isEmpty()) {
                for (int index = existing.size() - 1; index >= 0; index--) {
                    definition.removeCriteria(index);
                }
            }
            for (final SetsCriterion criterion : Iterables.transform(node.get("criteria"), new Function<JsonNode, SetsCriterion>() {
                @Override
                public SetsCriterion apply(final JsonNode node) {
                    final SetsCriterion criterion = new SetsCriterion();
                    criterion.setResolver(node.get("resolver").textValue());
                    criterion.setPayload(node.get("payload").textValue());
                    return criterion;
                }
            })) {
                try {
                    definition.addCriteria(criterion);
                } catch (Exception e) {
                    log.error("An error occurred trying to add criteria to the definition", e);
                }
            }
        }

        return definition;
    }

    private SetsDefinition getDefinition(final JsonNode node) {
        final String id = node.get("id").textValue();
        return SetsDefinition.getSetsDefinitionsById(id, null, false);
    }
}
