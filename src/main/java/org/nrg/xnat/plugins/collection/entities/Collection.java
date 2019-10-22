/*
 * xnat-template: org.nrg.xnat.plugins.template.entities.Template
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.plugins.collection.entities;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;

import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "id"))
public class Collection extends AbstractHibernateEntity {
    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    public String getProjectId() {
        return _projectId;
    }

    public void setProjectId(final String projectId) {
        _projectId = projectId;
    }


    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<String> getTrainingExperiments() {
        return _trainingExperiments;
    }

    public void setTrainingExperiments(List<String> _trainingExperiments) {
        this._trainingExperiments = _trainingExperiments;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<String> getValidationExperiments() {
        return _validationExperiments;
    }

    public void setValidationExperiments(List<String> _validationExperiments) {
        this._validationExperiments = _validationExperiments;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @Fetch(value = FetchMode.SUBSELECT)
    public List<String> getTestExperiments() {
        return _testExperiments;
    }

    public void setTestExperiments(List<String> _testExperiments) {
        this._testExperiments = _testExperiments;
    }

    private String _name;
    private String _projectId;
    private String _description;
    private List<String> _trainingExperiments;
    private List<String> _validationExperiments;
    private List<String> _testExperiments;
}
