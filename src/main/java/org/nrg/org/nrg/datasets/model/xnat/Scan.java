package org.nrg.org.nrg.datasets.model.xnat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class Scan extends DatasetElement implements Serializable {

    @JsonProperty("scan-type") private String scanType;
    @JsonProperty("series-instance-uid") private String seriesInstanceUID;
    @JsonProperty("acquisition-time") private String acquisitionTime;
    @JsonProperty("image-count") private String imageCount;
    private String path;
    private List<Resource> resources;
    private Integer frames;
    private String note;
    private String modality;
    private String quality;
    private String scanner;
    @JsonProperty("scanner-manufacturer") private String scannerManufacturer;
    @JsonProperty("scanner-model") private String scannerModel;
    @JsonProperty("scanner-software-version") private String scannerSoftwareVersion;
    @JsonProperty("series-description") private String seriesDescription;
    @JsonProperty("start-time") private Object startTime;

}
