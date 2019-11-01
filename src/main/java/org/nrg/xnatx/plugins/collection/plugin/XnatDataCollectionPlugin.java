/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.plugin.XnatCollectionPlugin
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.plugin;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@XnatPlugin(value = "dataCollectionPlugin", name = "XNAT Data Collection Plugin", entityPackages = "org.nrg.xnatx.plugins.collection.entities")
@ComponentScan({"org.nrg.xnatx.plugins.collection.daos",
                "org.nrg.xnatx.plugins.collection.rest",
                "org.nrg.xnatx.plugins.collection.services.impl"})
@Slf4j
public class XnatDataCollectionPlugin {
    public XnatDataCollectionPlugin() {
        log.info("Creating the XnatDataCollectionPlugin configuration class");
    }
}
