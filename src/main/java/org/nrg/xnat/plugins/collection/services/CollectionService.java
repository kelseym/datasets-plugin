package org.nrg.xnat.plugins.collection.services;

import org.nrg.framework.orm.hibernate.BaseHibernateService;
import org.nrg.xnat.plugins.collection.entities.Collection;

import java.util.List;

public interface CollectionService extends BaseHibernateService<Collection> {
    Collection findById(final String collectionId);
    List<Collection> getAllByProject(final String projectId);
}