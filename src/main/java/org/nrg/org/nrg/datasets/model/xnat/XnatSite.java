package org.nrg.org.nrg.datasets.model.xnat;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class XnatSite extends DatasetElement implements Serializable {

    private String url;
    private String description;
}