/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.services.CollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.services;

import java.util.Map;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.collection.entities.DataCollection;

import java.util.List;

public interface DataCollectionService extends BaseHibernateService<DataCollection> {
    DataCollection findById(final String collectionId);
    List<DataCollection> getAllByProject(final String projectId);
    Map<String, List<Map<String, String>>> getCollectionResources(final UserI user, final long id) throws NotFoundException;
}
