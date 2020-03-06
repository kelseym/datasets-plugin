/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.plugin.XnatCollectionPlugin
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xnatx.plugins.collection.converters.DataCollectionCriterionDeserializer;
import org.nrg.xnatx.plugins.collection.converters.DataCollectionCriterionSerializer;
import org.nrg.xnatx.plugins.collection.converters.DataCollectionDefinitionDeserializer;
import org.nrg.xnatx.plugins.collection.converters.DataCollectionDefinitionSerializer;
import org.nrg.xnatx.plugins.collection.converters.DataCollectionDeserializer;
import org.nrg.xnatx.plugins.collection.converters.DataCollectionSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@XnatPlugin(value = "dataCollectionPlugin", name = "XNAT Data Collection Plugin",
            dataModels = {@XnatDataModel(value = SetsDefinition.SCHEMA_ELEMENT_NAME,
                                         singular = "Dataset Definition",
                                         plural = "Dataset Definitions"),
                          @XnatDataModel(value = SetsCollection.SCHEMA_ELEMENT_NAME,
                                         singular = "Dataset Collection",
                                         plural = "Dataset Collections")})
@ComponentScan({"org.nrg.xnatx.plugins.collection.converters",
                "org.nrg.xnatx.plugins.collection.resolvers",
                "org.nrg.xnatx.plugins.collection.rest",
                "org.nrg.xnatx.plugins.collection.services.impl"})
@Slf4j
public class XnatDataCollectionPlugin {
    public XnatDataCollectionPlugin() {
        log.info("Creating the XnatDataCollectionPlugin configuration class");
    }

    @Bean
    public Module dataCollectionModule() {
        final SimpleModule module = new SimpleModule();
        module.addSerializer(SetsDefinition.class, new DataCollectionDefinitionSerializer());
        module.addSerializer(SetsCollection.class, new DataCollectionSerializer());
        module.addSerializer(SetsCriterion.class, new DataCollectionCriterionSerializer());
        module.addDeserializer(SetsDefinition.class, new DataCollectionDefinitionDeserializer());
        module.addDeserializer(SetsCollection.class, new DataCollectionDeserializer());
        module.addDeserializer(SetsCriterion.class, new DataCollectionCriterionDeserializer());
        return module;
    }
}
