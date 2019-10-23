/*
 * xnat-template: org.nrg.xnat.plugins.template.entities.Template
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.plugins.collection.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.nrg.framework.orm.hibernate.AbstractHibernateEntity;

import javax.persistence.*;
import java.util.List;

@ApiModel(description = "Contains the properties needed to create a collection.")
public class CollectionModel {

    @ApiModelProperty
    public String getName() {
        return _name;
    }

    public void setName(final String name) {
        _name = name;
    }

    @ApiModelProperty
    public String getProjectId() {
        return _projectId;
    }

    public void setProjectId(final String projectId) {
        _projectId = projectId;
    }

    @ApiModelProperty
    public String getDescription() {
        return _description;
    }

    public void setDescription(final String description) {
        _description = description;
    }

    @ApiModelProperty
    public List<String> getExperiments() {
        return _experiments;
    }

    public void setExperiments(List<String> _experiments) {
        this._experiments = _experiments;
    }

    @Override
    public String toString() {
        return "CollectionModel{" +
                "_name='" + _name + '\'' +
                ", _projectId='" + _projectId + '\'' +
                ", _description='" + _description + '\'' +
                ", _experiments=" + _experiments +
                '}';
    }

    public CollectionModel(String name, String projectId, String description, List<String> experiments) {
        _name = name;
        _projectId = projectId;
        _description = description;
        _experiments = experiments;
    }

    public CollectionModel() {

    }

    private String _name;
    private String _projectId;
    private String _description;
    private List<String> _experiments;
}