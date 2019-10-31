/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.plugin.XnatCollectionPlugin
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.plugin;

import org.nrg.framework.annotations.XnatPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@XnatPlugin(value = "collectionPlugin", name = "XNAT 1.7 Collection Plugin", entityPackages = "org.nrg.xnat.plugins.collection.entities")
@ComponentScan({"org.nrg.xnat.plugins.collection.daos",
        "org.nrg.xnat.plugins.collection.rest",
        "org.nrg.xnat.plugins.collection.services.impl"})
public class XnatDataCollectionPlugin {
    public XnatDataCollectionPlugin() {
        _log.info("Creating the XnatCollectionPlugin configuration class");
    }

    private static final Logger _log = LoggerFactory.getLogger(XnatDataCollectionPlugin.class);
}
