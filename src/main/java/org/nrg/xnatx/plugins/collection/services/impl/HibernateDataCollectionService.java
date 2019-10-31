/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.services.impl.HibernateCollectionService
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnatx.plugins.collection.daos.DataCollectionDAO;
import org.nrg.xnatx.plugins.collection.entities.DataCollection;
import org.nrg.xnatx.plugins.collection.services.DataCollectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages {@link DataCollection} data objects in Hibernate.
 */
@Service
public class HibernateDataCollectionService extends AbstractHibernateEntityService<DataCollection, DataCollectionDAO> implements DataCollectionService {
    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public DataCollection findById(final String collectionId) {
        return getDao().findByUniqueProperty("id", collectionId);
    }

    @Transactional
    @Override
    public List<DataCollection> getAllByProject(final String projectId) {
        return getDao().getCollectionsByProjectId(projectId);
    }
}
