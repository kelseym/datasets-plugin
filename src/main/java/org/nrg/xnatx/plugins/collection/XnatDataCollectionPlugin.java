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
import org.nrg.xnatx.plugins.collection.converters.CollectionDeserializer;
import org.nrg.xnatx.plugins.collection.converters.CollectionSerializer;
import org.nrg.xnatx.plugins.collection.converters.CriterionDeserializer;
import org.nrg.xnatx.plugins.collection.converters.CriterionSerializer;
import org.nrg.xnatx.plugins.collection.converters.DefinitionDeserializer;
import org.nrg.xnatx.plugins.collection.converters.DefinitionSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@XnatPlugin(value = "dataCollectionPlugin", name = "XNAT Data Collection Plugin", logConfigurationFile = "datasets-logback.xml",
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
