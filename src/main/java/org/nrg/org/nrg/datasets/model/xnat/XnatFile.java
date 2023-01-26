package org.nrg.org.nrg.datasets.model.xnat;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class XnatFile extends DatasetElement implements Serializable {

    private String name;
    private String path;
    private List<String> tags;
    private String format;
    private String content;
    private Long size;
    private String checksum;


}
