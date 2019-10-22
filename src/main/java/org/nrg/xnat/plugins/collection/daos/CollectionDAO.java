package org.nrg.xnat.plugins.collection.daos;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.nrg.framework.orm.hibernate.AbstractHibernateDAO;
import org.nrg.xnat.plugins.collection.entities.Collection;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by mike on 10/21/19.
 */
@Repository
public class CollectionDAO  extends AbstractHibernateDAO<Collection> {


    @SuppressWarnings("unchecked")
    @Transactional
    public List<Collection> getCollectionsByProjectId(String projectId){
        final Criteria criteria = getSession().createCriteria(getParameterizedType());
        criteria.add(Restrictions.eq("projectId", projectId));
        return criteria.list();
    }
}
