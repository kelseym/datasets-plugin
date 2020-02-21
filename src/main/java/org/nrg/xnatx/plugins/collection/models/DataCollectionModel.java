/*
 * xnatx-clara: org.nrg.xnatx.plugins.collection.model.CollectionModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.collection.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@ApiModel(description = "Contains the properties needed to create a collection.")
@Data
@Accessors(prefix = "_")
@AllArgsConstructor
@NoArgsConstructor
public class DataCollectionModel {
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

    @ApiModelProperty
    private String _name;

    @ApiModelProperty
    private String _projectId;

    @ApiModelProperty
    private String _description;

    @ApiModelProperty
    private String _imagesSeriesDescription;

    @ApiModelProperty
    private String _labelsSeriesDescription;

    @ApiModelProperty
    private List<String> _experiments;
}
