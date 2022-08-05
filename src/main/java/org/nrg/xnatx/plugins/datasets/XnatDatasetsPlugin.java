/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.XnatDatasetsPlugin
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatDataModel;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.xdat.model.SetsCollectionI;
import org.nrg.xdat.model.SetsCriterionI;
import org.nrg.xdat.model.SetsDefinitionI;
import org.nrg.xdat.om.SetsCollection;
import org.nrg.xdat.om.SetsCriterion;
import org.nrg.xdat.om.SetsDefinition;
import org.nrg.xnatx.plugins.datasets.converters.CollectionDeserializer;
import org.nrg.xnatx.plugins.datasets.converters.CollectionSerializer;
import org.nrg.xnatx.plugins.datasets.converters.CriterionDeserializer;
import org.nrg.xnatx.plugins.datasets.converters.CriterionSerializer;
import org.nrg.xnatx.plugins.datasets.converters.DefinitionDeserializer;
import org.nrg.xnatx.plugins.datasets.converters.DefinitionSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@XnatPlugin(value = "datasetsPlugin", name = "XNAT Datasets Plugin", logConfigurationFile = "datasets-logback.xml",
            dataModels = {@XnatDataModel(value = SetsDefinition.SCHEMA_ELEMENT_NAME,
                                         singular = "Dataset Definition",
                                         plural = "Dataset Definitions"),
                          @XnatDataModel(value = SetsCollection.SCHEMA_ELEMENT_NAME,
                                         singular = "Dataset Collection",
                                         plural = "Dataset Collections")})
@ComponentScan({"org.nrg.xnatx.plugins.datasets.converters",
                "org.nrg.xnatx.plugins.datasets.resolvers",
                "org.nrg.xnatx.plugins.datasets.rest",
                "org.nrg.xnatx.plugins.datasets.services.impl"})
@Slf4j
public class XnatDatasetsPlugin {
    @Bean
    public Module dataCollectionModule() {
        final SimpleModule module = new SimpleModule();

        final SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(SetsCollectionI.class, SetsCollection.class);
        resolver.addMapping(SetsDefinitionI.class, SetsDefinition.class);
        resolver.addMapping(SetsCriterionI.class, SetsCriterion.class);
        module.setAbstractTypes(resolver);

        module.addDeserializer(SetsCollection.class, new CollectionDeserializer());
        module.addDeserializer(SetsCriterion.class, new CriterionDeserializer());
        module.addDeserializer(SetsDefinition.class, new DefinitionDeserializer());
        module.addSerializer(SetsCollection.class, new CollectionSerializer());
        module.addSerializer(SetsCriterion.class, new CriterionSerializer());
        module.addSerializer(SetsDefinition.class, new DefinitionSerializer());
        return module;
    }
}
