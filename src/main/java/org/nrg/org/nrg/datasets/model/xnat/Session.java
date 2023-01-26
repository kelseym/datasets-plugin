package org.nrg.org.nrg.datasets.model.xnat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.nrg.xnat.eventservice.model.xnat.Assessor;
import org.nrg.xnat.eventservice.model.xnat.Resource;
import org.nrg.xnat.eventservice.model.xnat.Scan;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Session extends DatasetElement implements Serializable {

    private List<Scan> scans;
    private List<Assessor> assessors;
    private List<Resource> resources;
    @JsonProperty("project-id") private String project;
    @JsonProperty("subject-id") private String subject;
    private String directory;
    @JsonProperty("modalities-in-study") private List<String> modalities;


}
