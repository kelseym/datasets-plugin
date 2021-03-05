/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.resolvers.ProjectResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.resolvers;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(prefix = "_")
public class ProjectResource {
    @Override
    public String toString() {
        return String.format(FORMAT, _subjectId, _experimentId, _scanId, _resourceId, _subjectLabel, _experimentLabel, _dataType, _scanType, _seriesDescription, _seriesClass, _resourceLabel, _resourceContent, _resourceFormat, _resourceDescription, _experimentLastModified, _resourceLastModified, _resourceFileCount, _resourceSize);
    }

    private static final String FORMAT = "{ \"subjectId\": \"%s\", \"experimentId\": \"%s\", \"scanId\": \"%s\", \"resourceId\": %d, \"subjectLabel\": \"%s\", \"experimentLabel\": \"%s\", \"dataType\": \"%s\", \"scanType\": \"%s\", \"seriesDescription\": \"%s\", \"seriesClass\": \"%s\", \"resourceLabel\": \"%s\", \"resourceContent\": \"%s\", \"resourceFormat\": \"%s\", \"resourceDescription\": \"%s\", \"experimentLastModified\": \"%tc\", \"resourceLastModified\": \"%tc\", \"resourceFileCount\": %d, \"resourceSize\": %d }";

    private String _subjectId;
    private String _experimentId;
    private String _scanId;
    private int    _resourceId;
    private String _subjectLabel;
    private String _experimentLabel;
    private String _dataType;
    private String _scanType;
    private String _seriesDescription;
    private String _seriesClass;
    private String _resourceLabel;
    private String _resourceContent;
    private String _resourceFormat;
    private String _resourceDescription;
    private Date   _experimentLastModified;
    private Date   _resourceLastModified;
    private int    _resourceFileCount;
    private long   _resourceSize;
}
