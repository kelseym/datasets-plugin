package org.nrg.org.nrg.datasets.model.xnat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.nrg.xnat.eventservice.model.xnat.Resource;
import org.nrg.xnat.eventservice.model.xnat.Session;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Subject extends DatasetElement implements Serializable {

    private List<Session> sessions;
    private List<Resource> resources;
    @JsonProperty("project-id") private String projectId;
    @JsonProperty("group") private String group;
    @JsonProperty("source") private String source;
    @JsonProperty("initials") private String initials;

}
