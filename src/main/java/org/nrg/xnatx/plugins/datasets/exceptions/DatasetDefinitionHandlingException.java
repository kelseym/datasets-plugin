/*
 * ml-plugin: org.nrg.xnatx.plugins.datasets.exceptions.DatasetDefinitionHandlingException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnatx.plugins.datasets.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DatasetDefinitionHandlingException extends BaseDatasetException {
    public DatasetDefinitionHandlingException(final String message) {
        super(message);
    }

    public DatasetDefinitionHandlingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
