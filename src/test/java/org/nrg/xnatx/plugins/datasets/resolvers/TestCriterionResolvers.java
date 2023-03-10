/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.resolvers.TestCriterionResolvers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.resolvers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.nrg.xnatx.plugins.datasets.resolvers.ExpressionResolver.getExpression;
import static org.nrg.xnatx.plugins.datasets.resolvers.ExpressionResolver.getExpressions;
import static org.nrg.xnatx.plugins.datasets.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_CONTENT;
import static org.nrg.xnatx.plugins.datasets.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_FORMAT;
import static org.nrg.xnatx.plugins.datasets.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_LABEL;
import static org.nrg.xnatx.plugins.datasets.resolvers.SeriesAndResourceCriterionResolver.SERIES_DESCRIPTION;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCriterionResolversConfig.class})
@Slf4j
public class TestCriterionResolvers {
    @Autowired
    public void setSerializerService(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Test
    public void testCriterionResolver() throws IOException {
        final JsonNode json = _serializer.deserializeJson(DEFINITION_JSON);

        final List<List<String>> clauses = new ArrayList<>();
        for (final String element : EXPRESSION_ATTRIBUTES.keySet()) {
            if (json.has(element)) {
                final JsonNode node = json.get(element);
                switch (node.getNodeType()) {
                    case ARRAY:
                        clauses.add(getExpressions(EXPRESSION_ATTRIBUTES.get(element), ExpressionResolver.arrayNodeToStrings(node)));
                        break;
                    case STRING:
                    case OBJECT:
                        clauses.add(getExpression(EXPRESSION_ATTRIBUTES.get(element), node.textValue()));
                        break;
                    default:
                        log.warn("Skipping unknown JSON node type for {}: {}", element, node.getNodeType());
                }
            }
        }
        assertThat(clauses).isNotNull().isNotEmpty();
        final String resolved = ExpressionResolver.joinClauses(clauses);
        assertThat(resolved).isNotBlank().isEqualTo(RESOLVED_CLAUSE);
    }

    private static final String                       RESOLVED_CLAUSE               = "(scan_type = 'MPRAGE T1 AX' OR series_description = 'MPRAGE T1 AX' OR series_class = 'MPRAGE T1 AX' OR scan_type ~ '^.T2[[:space:]]+FLAIR.$' OR series_description ~ '^.T2[[:space:]]+FLAIR.$' OR series_class ~ '^.T2[[:space:]]+FLAIR.$') AND (resource_format = 'NIFTI' OR resource_format = 'BIDS' OR resource_format ~ '^JPE?G$') AND (resource_content ~* 'infarct ROIS' OR resource_content = '4dfp') AND (resource_label = 'SNAPSHOTS' OR resource_label ~* 'nifti')";
    private static final String                       DEFINITION_JSON               = "{ \"SeriesDescription\": [ \"MPRAGE T1 AX\", \"/^.T2[[:space:]]+FLAIR.$/\" ], \"ResourceFormat\": [ \"NIFTI\", \"BIDS\", \"/^JPE?G$/\" ], \"ResourceContent\": [ \"/infarct ROIS/i\", \"4dfp\" ], \"ResourceLabel\": [ \"SNAPSHOTS\", \"/nifti/i\" ] }";
    private static final List<String>                 SERIES_DESCRIPTION_ATTRIBUTES = Arrays.asList("scan_type", "series_description", "series_class");
    private static final List<String>                 RESOURCE_LABEL_ATTRIBUTES     = Collections.singletonList("resource_label");
    private static final List<String>                 RESOURCE_CONTENT_ATTRIBUTES   = Collections.singletonList("resource_content");
    private static final List<String>                 RESOURCE_FORMAT_ATTRIBUTES    = Collections.singletonList("resource_format");
    private static final ListMultimap<String, String> EXPRESSION_ATTRIBUTES         = ImmutableListMultimap.<String, String>builder().putAll(SERIES_DESCRIPTION, SERIES_DESCRIPTION_ATTRIBUTES)
                                                                                                                                     .putAll(RESOURCE_FORMAT, RESOURCE_FORMAT_ATTRIBUTES)
                                                                                                                                     .putAll(RESOURCE_CONTENT, RESOURCE_CONTENT_ATTRIBUTES)
                                                                                                                                     .putAll(RESOURCE_LABEL, RESOURCE_LABEL_ATTRIBUTES)
                                                                                                                                     .build();

    private SerializerService _serializer;
}
