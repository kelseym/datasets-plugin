/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.exceptions.DatasetCriterionResolverException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class DatasetCriterionResolverException extends BaseDatasetException {
    public DatasetCriterionResolverException(final String message) {
        super(message);
    }

    public DatasetCriterionResolverException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
