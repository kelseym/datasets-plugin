/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.converters.TestCollectionDeserializer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.converters;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.om.SetsCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestSerializationConfig.class)
@Slf4j
public class TestCollectionDeserializer {
    @Autowired
    public void setSerializer(final SerializerService serializer) {
        _serializer = serializer;
    }

    @Test
    @Ignore("This needs a mock for XnatAbstractresource.getXnatAbstractresourcesByXnatAbstractresourceId() so that returns something for the (non-existent) references")
    public void testDeserialization() throws IOException {
        final SetsCollection collection = _serializer.deserializeJson("{\"id\": \"XNAT_E00001\", \"label\": \"Test Thing\", \"references\": [1, 2, 3]}", SetsCollection.class);
        assertThat(collection.getReferences_resource()).hasSize(3);
    }

    private SerializerService _serializer;
}
