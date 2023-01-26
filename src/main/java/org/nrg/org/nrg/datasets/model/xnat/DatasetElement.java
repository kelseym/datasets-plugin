package org.nrg.org.nrg.datasets.model.xnat;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "objectType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = XnatSite.class, name = "site"),
        @JsonSubTypes.Type(value = Project.class, name = "project"),
        @JsonSubTypes.Type(value = Subject.class, name = "subject"),
        @JsonSubTypes.Type(value = Session.class, name = "session"),
        @JsonSubTypes.Type(value = Scan.class, name = "scan"),
        @JsonSubTypes.Type(value = Resource.class, name = "resource"),
        @JsonSubTypes.Type(value = XnatFile.class, name = "file")
})public abstract class DatasetElement implements Serializable {

    protected String id;
    protected String label;
    protected String xsiType;
    protected String uri;


}
