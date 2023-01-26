package org.nrg.org.nrg.datasets.model.xnat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.nrg.xnat.eventservice.model.xnat.ProjectAsset;
import org.nrg.xnat.eventservice.model.xnat.Resource;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Project extends DatasetElement implements Serializable {

    private List<Resource> resources;
    private List<Subject> subjects;
    @JsonProperty("project-assets") private List<ProjectAsset> projectAssets;
    private String path;
    private String title;
    @JsonProperty("running-title") private String runningTitle;
    private String description;
    private String keywords;
    private String accessibility;
    private List<String> aliases;
    private String pi;

}