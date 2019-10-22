package org.nrg.xnat.plugins.collection.services.impl;

import org.nrg.framework.orm.hibernate.AbstractHibernateEntityService;
import org.nrg.xnat.plugins.collection.daos.CollectionDAO;
import org.nrg.xnat.plugins.collection.entities.Collection;
import org.nrg.xnat.plugins.collection.services.CollectionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages {@link Collection} data objects in Hibernate.
 */
@Service
public class HibernateCollectionService extends AbstractHibernateEntityService<Collection, CollectionDAO> implements CollectionService {
    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Collection findById(final String collectionId) {
        return getDao().findByUniqueProperty("id", collectionId);
    }

    @Transactional
    @Override
    public List<Collection> getAllByProject(final String projectId) {
        return getDao().getCollectionsByProjectId(projectId);
    }
}
