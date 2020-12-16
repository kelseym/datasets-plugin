/*
 * Clara Plugin: org.nrg.xnatx.plugins.collection.resolvers.TestJsonIteration
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.resolvers;


import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_CONTENT;
import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_FORMAT;
import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.RESOURCE_LABEL;
import static org.nrg.xnatx.plugins.collection.resolvers.SeriesAndResourceCriterionResolver.SERIES_DESCRIPTION;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCriterionResolversConfig.class})
@Slf4j
public class TestJsonIteration {
    @Autowired
    public void setSerializerService(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Test
    public void testJsonElements() throws IOException {
        final JsonNode         json     = _serializer.deserializeJson(DEFINITION_JSON);
        final Iterator<String> iterator = json.fieldNames();
        while (iterator.hasNext()) {
            final String   fieldName = iterator.next();
            final JsonNode node      = json.get(fieldName);
            final String   tagName   = node.has("tag") ? node.get("tag").textValue() : StringUtils.uncapitalize(fieldName);
        }
    }

    private static final String                       DEFINITION_JSON               = "{\"Images\": {\"tag\": \"image\", \"SeriesDescription\": [\"T1%\"], \"ResourceFormat\": [\"NIFTI\"], \"ResourceContent\": [\"/T1./i\"], \"ResourceLabel\": [\"/nifti/i\"]}, \"Labels\": {\"tag\": \"label\", \"SeriesDescription\": [\"Segment%\"], \"ResourceFormat\": [\"NIFTI\"], \"ResourceContent\": [\"/Segmentat.{3}/i\"], \"ResourceLabel\": [\"/nifti/i\"]}}";
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
