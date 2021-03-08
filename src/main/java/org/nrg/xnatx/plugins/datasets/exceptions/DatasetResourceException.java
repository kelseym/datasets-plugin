/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.exceptions.DatasetResourceException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.exceptions;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
@Accessors(prefix = "_")
@Slf4j
public class DatasetResourceException extends BaseDatasetException {
    public DatasetResourceException(final String message, final List<XnatAbstractresourceI> resources) {
        super(message);
        _resources.addAll(resources);
    }

    private final List<XnatAbstractresourceI> _resources = new ArrayList<>();
}
