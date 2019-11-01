/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.entities.Collection
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.entities;

import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "nrg")
public class DataCollection extends AbstractHibernateEntity {
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

    public String getImagesSeriesDescription() {
        return _imagesSeriesDescription;
    }

    public void setImagesSeriesDescription(String _imagesSeriesDescription) {
        this._imagesSeriesDescription = _imagesSeriesDescription;
    }

    public String getLabelsSeriesDescription() {
        return _labelsSeriesDescription;
    }

    public void setLabelsSeriesDescription(String _labelsSeriesDescription) {
        this._labelsSeriesDescription = _labelsSeriesDescription;
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
    private String _imagesSeriesDescription;
    private String _labelsSeriesDescription;
    private List<String> _trainingExperiments;
    private List<String> _validationExperiments;
    private List<String> _testExperiments;
}
