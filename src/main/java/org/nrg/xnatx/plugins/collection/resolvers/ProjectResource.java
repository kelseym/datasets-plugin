package org.nrg.xnatx.plugins.collection.resolvers;

import java.util.Date;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(prefix = "_")
public class ProjectResource {
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
