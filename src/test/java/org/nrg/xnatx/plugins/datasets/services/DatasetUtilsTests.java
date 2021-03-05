/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.services.DatasetUtilsTests
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.BasicXnatResourceLocator;
import org.nrg.xnatx.plugins.datasets.services.DatasetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SerializerConfig.class)
public class DatasetUtilsTests {
    @Test
    public void testCollectionPartitioning() throws IOException {
        final List<Pair<String, String>> imageAndLabelPairs = new ArrayList<>();

        final JsonNode collection = _serializer.deserializeJson(IOUtils.toString(BasicXnatResourceLocator.getResource("collection.json").getInputStream(), StandardCharsets.UTF_8));
        for (final JsonNode node : collection) {
            imageAndLabelPairs.add(Pair.of(node.get("image").textValue(), node.get("label").textValue()));
        }

        assertThat(imageAndLabelPairs).size().isEqualTo(100);

        final Map<String, List<Pair<String, String>>> partitions = DatasetUtils.partition(imageAndLabelPairs, ImmutableMap.of("training", 70, "validation", 20, "test", 10));
        final Collection<Pair<String, String>>        training   = partitions.get("training");
        final Collection<Pair<String, String>>        validation = partitions.get("validation");
        final List<String> test = partitions.get("test").stream().map(Pair::getKey).collect(Collectors.toList());

        assertThat(training).size().isEqualTo(70);
        assertThat(validation).size().isEqualTo(20);
        assertThat(test).size().isEqualTo(10);
    }

    @Autowired
    private SerializerService _serializer;
}
