/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.converters.TestSerializationConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.converters;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.nrg.framework.configuration.SerializerConfig;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xnatx.plugins.datasets.converters.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SerializerConfig.class)
public class TestSerializationConfig {
    @Bean
    public Module dataCollectionModule() {
        final SimpleModule module = new SimpleModule();
        module.addDeserializer(SetsCollection.class, new CollectionDeserializer());
        module.addDeserializer(SetsCriterion.class, new CriterionDeserializer());
        module.addDeserializer(SetsDefinition.class, new DefinitionDeserializer());
        module.addSerializer(SetsCollection.class, new CollectionSerializer());
        module.addSerializer(SetsCriterion.class, new CriterionSerializer());
        module.addSerializer(SetsDefinition.class, new DefinitionSerializer());
        return module;
    }
}
