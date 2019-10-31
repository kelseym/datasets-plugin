/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.model.CollectionModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.examples;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(description = "Contains the properties needed to create a collection.")
public class DataCollectionExample {

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
    public String getImagesSeriesDescription() {
        return _imagesSeriesDescription;
    }

    public void setImagesSeriesDescription(final String imagesSeriesDescription) {
        _imagesSeriesDescription = imagesSeriesDescription;
    }

    @ApiModelProperty
    public String getLabelsSeriesDescription() {
        return _labelsSeriesDescription;
    }

    public void setLabelsSeriesDescription(final String labelsSeriesDescription) {
        _labelsSeriesDescription = labelsSeriesDescription;
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
                ", _imagesSeriesDescription='" + _imagesSeriesDescription + '\'' +
                ", _labelsSeriesDescription='" + _labelsSeriesDescription + '\'' +
                ", _experiments=" + _experiments +
                '}';
    }

    public DataCollectionExample(String name, String projectId, String description, String imagesSeriesDescription, String labelsSeriesDescription, List<String> experiments) {
        _name = name;
        _projectId = projectId;
        _description = description;
        _imagesSeriesDescription = imagesSeriesDescription;
        _labelsSeriesDescription = labelsSeriesDescription;
        _experiments = experiments;
    }

    public DataCollectionExample() {

    }

    private String _name;
    private String _projectId;
    private String _description;
    private String _imagesSeriesDescription;
    private String _labelsSeriesDescription;
    private List<String> _experiments;
}
