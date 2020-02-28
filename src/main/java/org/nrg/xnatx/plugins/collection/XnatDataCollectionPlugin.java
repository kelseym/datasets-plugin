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
import org.nrg.xdat.model.XnatxDatacollectionI;
import org.nrg.xdat.model.XnatxDatacollectioncriterionI;
import org.nrg.xdat.model.XnatxDatacollectiondefinitionI;
import org.nrg.xdat.om.XnatxDatacollection;
import org.nrg.xdat.om.XnatxDatacollectiondefinition;
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
            dataModels = {@XnatDataModel(value = XnatxDatacollectiondefinition.SCHEMA_ELEMENT_NAME,
                                         singular = "Data Collection Definition",
                                         plural = "Data Collection Definitions"),
                          @XnatDataModel(value = XnatxDatacollection.SCHEMA_ELEMENT_NAME,
                                         singular = "Data Collection",
                                         plural = "Data Collections")})
@ComponentScan({"org.nrg.xnatx.plugins.collection.converters",
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
        module.addSerializer(XnatxDatacollectiondefinitionI.class, new DataCollectionDefinitionSerializer());
        module.addSerializer(XnatxDatacollectionI.class, new DataCollectionSerializer());
        module.addSerializer(XnatxDatacollectioncriterionI.class, new DataCollectionCriterionSerializer());
        module.addDeserializer(XnatxDatacollectiondefinitionI.class, new DataCollectionDefinitionDeserializer());
        module.addDeserializer(XnatxDatacollectionI.class, new DataCollectionDeserializer());
        module.addDeserializer(XnatxDatacollectioncriterionI.class, new DataCollectionCriterionDeserializer());
        return module;
    }
}
