package org.nrg.xnatx.plugins.collection.resolvers;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(prefix = "_")
@EqualsAndHashCode(callSuper = true)
public class ProjectResourceReport extends ProjectResource {
    private String  _tag;
    private Boolean _subjectLabelMatches;
    private Boolean _experimentLabelMatches;
    private Boolean _scanTypeMatches;
    private Boolean _seriesDescriptionMatches;
    private Boolean _seriesClassMatches;
    private Boolean _dataTypeMatches;
    private Boolean _resourceLabelMatches;
    private Boolean _resourceContentMatches;
    private Boolean _resourceFormatMatches;
    private Boolean _resourceDescriptionMatches;
    private Boolean _resourceLastModifiedMatches;
    private Boolean _resourceFileCountMatches;
    private Boolean _resourceSizeMatches;
}
