/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.daos.CollectionDAO
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnatx.plugins.collection.entities.DataCollection;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by mike on 10/21/19.
 */
@Repository
public class DataCollectionDAO extends AbstractHibernateDAO<DataCollection> {


    @SuppressWarnings("unchecked")
    @Transactional
    public List<DataCollection> getCollectionsByProjectId(String projectId){
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("projectId", projectId));
        return criteria.list();
    }
}
